;
; Copyright © 2021 Peter Monks
;
; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.
;
; SPDX-License-Identifier: MPL-2.0
;

(ns bot.main
  (:require [CLJ-2253]
            [bot.config            :as cfg]
            [clojure.string        :as s]
            [clojure.java.io       :as io]
            [clojure.tools.cli     :as cli]
            [clojure.tools.logging :as log]
            [mount.core            :as mnt]
            [java-time             :as tm]
            [discljord.events      :as de]
            [discljord-utils.util  :as u]
            [bot.commands          :as cmd]
            [bot.http])     ; Ensure the HTTP server gets started by mount
  (:gen-class))

(def ^:private cli-opts
  [["-c" "--config-file FILE" "Path to configuration file (defaults to 'config.edn' in the classpath)"
    :validate [#(.exists (io/file %)) "Must exist"
               #(.isFile (io/file %)) "Must be a file"]]
   ["-h" "--help"]])

(defn- usage
  "Returns usage instructions for running the bot."
  [options-summary]
  (s/join
    \newline
    ["Usage: bot [options]"
     ""
     "Options:"
     options-summary
     ""]))

(defn -main
  "Runs the bot."
  [& args]
  (try
    (let [{:keys [options errors summary]} (cli/parse-opts args cli-opts)]
      (cond
        (:help options) (u/exit 0 (usage summary))
        errors          (u/exit 1 (str "The following errors occurred while parsing the command line:\n\n"
                                       (s/join \newline errors))))

      ; Start the bot
      (log/info "Starting on Clojure" (clojure-version) "/" (System/getProperty "java.vm.vendor") "JVM" (System/getProperty "java.vm.version") (str "(" (System/getProperty "os.name") "/" (System/getProperty "os.arch") ")"))
      (mnt/with-args options)
      (mnt/start)
      (let [bot-name (get-in cfg/config [:bot :name])]
        (when cfg/build-info
          (log/info (str bot-name
                    " built at " (tm/format :iso-instant (:build-date cfg/build-info))
                    (when (:build-url cfg/build-info) (str " from " (:build-url cfg/build-info))))))
        (log/info (str bot-name " started"))
        (de/message-pump! (:discord-event-channel cfg/config) cmd/handle-discord-event)))   ; This must go last, as it blocks
    (catch Exception e
      (u/log-exception e)
      (u/exit -1)))
  (u/exit))
