;
; Copyright © 2021 Peter Monks
;
; Licensed under the Apache License, Version 2.0 (the "License");
; you may not use this file except in compliance with the License.
; You may obtain a copy of the License at
;
;     http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
;
; SPDX-License-Identifier: Apache-2.0
;

(ns bot.commands
  (:require [clojure.string               :as s]
            [clojure.tools.logging        :as log]
            [java-time                    :as tm]
            [mount.core                   :as mnt :refer [defstate]]
            [discljord-utils.util         :as u]
            [discljord-utils.message-util :as mu]
            [bot.config                   :as cfg]))

(def prefix "!")

(defn embed-template
  "Generates a default template for embeds."
 []
 {:color     (get-in cfg/config [:bot :colour])
  :footer    {:text     (get-in cfg/config [:bot :name])
              :icon_url (get-in cfg/config [:bot :logo])}
  :timestamp (str (tm/instant))})

(defn privacy-command!
  "Provides a link to the bot's privacy policy"
  [_ event-data]
  (mu/create-message! (:discord-message-channel cfg/config)
                      (:channel-id event-data)
                      :embed (assoc (embed-template)
                                    :description "[ctac-bot's privacy policy is available here](https://github.com/pmonks/ctac-bot/blob/main/PRIVACY.md).")))

(defn status-command!
  "Provides technical status of the bot"
  [_ event-data]
  (let [now (tm/instant)]
    (mu/create-message! (:discord-message-channel cfg/config)
                        (:channel-id event-data)
                        :embed (assoc (embed-template)
                                      :title "Status"
                                      :fields [
                                        {:name "Running for"            :value (str (u/human-readable-date-diff cfg/boot-time now))}
                                        {:name "Built at"               :value (str (tm/format :iso-instant (:build-date cfg/build-info))
                                                                                    (when (:repo cfg/build-info)
                                                                                      (str " from [" (if-let [tag (:tag cfg/build-info)] tag (:sha cfg/build-info)) "](" (:build-url cfg/build-info) ")")))}

                                        ; Table of fields here
                                        {:name "Clojure"                :value (str "v" (clojure-version)) :inline true}
                                        {:name "JVM"                    :value (str (System/getProperty "java.vm.vendor") " v" (System/getProperty "java.vm.version") " (" (System/getProperty "os.name") "/" (System/getProperty "os.arch") ")") :inline true}
                                        ; Force a newline (Discord is hardcoded to show 3 fields per line), by using Unicode zero width spaces (empty/blank strings won't work!)
                                        {:name "​"                       :value "​" :inline true}
                                        {:name "Heap memory in use"     :value (u/human-readable-size (.getUsed (.getHeapMemoryUsage (java.lang.management.ManagementFactory/getMemoryMXBean)))) :inline true}
                                        {:name "Non-heap memory in use" :value (u/human-readable-size (.getUsed (.getNonHeapMemoryUsage (java.lang.management.ManagementFactory/getMemoryMXBean)))) :inline true}
                                      ]))))

(defn gc-command!
  "Requests that the bot's JVM perform a GC cycle."
  [_ event-data]
  (System/gc)
  (mu/create-message! (:discord-message-channel cfg/config)
                      (:channel-id event-data)
                      :content "Garbage collection requested."))

(defn set-logging-command!
  "Sets the log level for the given logger"
  [args event-data]
  (let [[level logger] (s/split args #"\s+")]
    (if (and (not (s/blank? level))
             (not (s/blank? logger)))
      (do
        (cfg/set-log-level! level logger)
        (mu/create-message! (:discord-message-channel cfg/config)
                            (:channel-id event-data)
                            :content (str "Logging level " (s/upper-case level) " set for logger '" logger "'.")))
      (mu/create-message! (:discord-message-channel cfg/config)
                          (:channel-id event-data)
                          :content "Logging level or logger not provided; logging level must be one of: ERROR, WARN, INFO, DEBUG, TRACE"))))

(defn debug-logging-command!
  "Enables debug logging, which turns on TRACE for 'discljord' and DEBUG for the bot's top-level namespace."
  [_ event-data]
  (let [bot-tlns (first (s/split (str (:bot-ns cfg/config)) #"\.+"))]
    (cfg/set-log-level! "TRACE" "discljord")
    (cfg/set-log-level! "DEBUG" bot-tlns)
    (mu/create-message! (:discord-message-channel cfg/config)
                        (:channel-id event-data)
                        :content (str "Debug logging enabled (TRACE for 'discljord' and DEBUG for '" bot-tlns "'."))))

(defn reset-logging-command!
  "Resets all log levels to their configured defaults."
  [_ event-data]
  (cfg/reset-logging!)
  (mu/create-message! (:discord-message-channel cfg/config)
                      (:channel-id event-data)
                      :content "Logging configuration reset."))


(declare help-command!)

(defn- find-bot-ns-commands
  "Find all public bot commands in the namespace represented by the given symbol, loading it if necessary"
  [ns-sym]
  (when ns-sym
    (require ns-sym)
    (into {} (for [[_ v] (ns-publics (find-ns ns-sym))]
               (when (:bot-command (meta v))
                 [(:bot-command meta v) v])))))

; Table of "public" commands; those that can be used in any channel, group or DM
(declare  public-command-dispatch-table)
(defstate public-command-dispatch-table
  :start (merge {"help"    #'help-command!
                 "privacy" #'privacy-command!}
                 (find-bot-ns-commands (get-in cfg/config [:bot :ns-sym]))))

; Table of "secret" commands; those that don't show up in the help and can only be used in a DM
(def secret-command-dispatch-table
  {"status"       #'status-command!
   "gc"           #'gc-command!
   "setlogging"   #'set-logging-command!
   "debuglogging" #'debug-logging-command!
   "resetlogging" #'reset-logging-command!})

(defn help-command!
  "Displays this help message"
  [_ event-data]
  (mu/create-message! (:discord-message-channel cfg/config)
                      (:channel-id event-data)
                      :embed (assoc (embed-template)
                                    :description (str "I understand the following command(s):\n"
                                                      (s/join "\n" (map #(str " • **`" prefix (key %) "`** - " (:doc (meta (val %))))
                                                                        (sort-by key public-command-dispatch-table)))))))

; Responsive fns
(defmulti handle-discord-event
  "Discord event handler"
  (fn [event-type _] event-type))

; Default Discord event handler (noop)
(defmethod handle-discord-event :default
  [_ _])

(defmethod handle-discord-event :message-create
  [_ event-data]
  ; Only respond to messages sent from a human
  (when (mu/human-message? event-data)
    (future    ; Spin off the actual processing, so we don't clog the Discord event queue
      (try
        (let [content (s/triml (:content event-data))]
          (if (s/starts-with? content prefix)
            ; Parse the requested command and call it, if it exists
            (let [command-and-args  (s/split content #"\s+" 2)
                  command           (s/lower-case (subs (s/trim (first command-and-args)) (count prefix)))
                  args              (second command-and-args)]
              (if-let [public-command-fn (get public-command-dispatch-table command)]
                (do
                  (log/debug (str "Calling public command fn for '" command "' with args '" args "'."))
                  (public-command-fn args event-data))
                (when (mu/direct-message? event-data)
                  (if-let [secret-command-fn (get secret-command-dispatch-table command)]
                    (do
                      (log/debug (str "Calling secret command fn for '" command "' with args '" args "'."))
                      (secret-command-fn args event-data))
                    (help-command! nil event-data)))))   ; If the requested secret command doesn't exist, provide help
            ; If any unrecognised message was sent to a DM channel, provide help
            (when (mu/direct-message? event-data)
              (help-command! nil event-data))))
        (catch Exception e
          (u/log-exception e))))))
