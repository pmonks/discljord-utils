;
; Copyright © 2026 Juan Pedrozo
;
; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.
;
; SPDX-License-Identifier: MPL-2.0
;

(ns bot.slash-commands
  "Support for Discord slash commands, paralleling bot.commands (which handles prefix commands).
  Bots that don't define them are unaffected.  When they are defined, the config must also contain :discord-application-id"
  (:require [clojure.tools.logging        :as log]
            [mount.core                   :as mnt :refer [defstate]]
            [embroidery.api               :as e]
            [slash.core                   :as slash]
            [slash.gateway                :as gtw]
            [discljord-utils.util         :as u]
            [discljord-utils.message-util :as mu]
            [bot.config                   :as cfg]
            [bot.commands                 :as cmd]))

(defn- slash-ns-var-value
  [var-name]
  (when-let [v (ns-resolve (get-in cfg/config [:bot :slash-ns-sym]) var-name)]
    (deref v)))

(declare slash-command-handlers)
(defstate slash-command-handlers
  :start (when-let [slash-ns-sym (get-in cfg/config [:bot :slash-ns-sym])]
           (require slash-ns-sym)
           (let [definitions (slash-ns-var-value 'slash-commands)
                 paths       (slash-ns-var-value 'slash-command-paths)]
             (when (and (seq definitions) paths)
               (if (:reload-command-definitions? cfg/config)
                 (let [application-id (cfg/validated-config-value cfg/config :discord-application-id)
                       registered     (mu/bulk-overwrite-global-application-commands! (:discord-message-channel cfg/config)
                                                                                      application-id
                                                                                      definitions)]
                   (log/info "Overwrote" (count registered) "slash command(s):" (pr-str (map :name registered))))
                 (log/info "Skipped overwriting of slash commands"))
               (assoc gtw/gateway-defaults :application-command paths)))))

(defmethod cmd/handle-discord-event :interaction-create
  [_ event-data]
  (when slash-command-handlers
    (e/future*
     (try
       (slash/route-interaction slash-command-handlers event-data)
       (catch Exception e
         (u/log-exception e))))))