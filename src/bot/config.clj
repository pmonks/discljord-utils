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

(ns bot.config
  (:require [clojure.java.io        :as io]
            [clojure.string         :as s]
            [clojure.edn            :as edn]
            [clojure.core.async     :as async]
            [java-time              :as tm]
            [aero.core              :as a]
            [mount.core             :as mnt :refer [defstate]]
            [discljord.connections  :as dc]
            [discljord.messaging    :as dm]
            [discljord-utils.util   :as u]))

; Because java.util.logging is a hot mess
(org.slf4j.bridge.SLF4JBridgeHandler/removeHandlersForRootLogger)
(org.slf4j.bridge.SLF4JBridgeHandler/install)

; Because Java's default exception behaviour in threads other than main is a hot mess
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ t e]
     (u/log-exception e (str "Uncaught exception on " (.getName t))))))

(def boot-time (tm/instant))

; Adds a #split reader macro to aero - see https://github.com/juxt/aero/issues/55
(defmethod a/reader 'split
  [_ _ value]
  (let [[s re] value]
    (when (and s re)
      (s/split s (re-pattern re)))))

(defn validated-config-value
  [m k]
  (let [result (get m k)]
    (if-not (s/blank? result)
      result
      (throw (ex-info (str "Config key '"(name k)"' not provided") {})))))

(declare  config)
(defstate config
          :start (let [raw-config            (if-let [config-file (:config-file (mnt/args))]
                                               (a/read-config config-file)
                                               (a/read-config (io/resource "config.edn")))
                       discord-api-token     (validated-config-value raw-config :discord-api-token)
                       discord-event-channel (async/chan (u/getrn raw-config :discord-event-channel-size 100))
                       bot-ns-name           (if-let [bot-ns-name (get-in raw-config [:bot :ns])] bot-ns-name (throw (ex-info "Config key '[:bot :ns]' not provided" {})))
                       bot-intents           (if-let [intents (get-in raw-config [:bot :intents])] intents #{:guilds :guild-messages :direct-messages})]
                   (into raw-config
                         {
                           :discord-event-channel      discord-event-channel
                           :discord-connection-channel (if-let [connection (dc/connect-bot! discord-api-token
                                                                                            discord-event-channel
                                                                                            :intents bot-intents)]
                                                         connection
                                                         (throw (ex-info "Failed to connect bot to Discord" {})))
                           :discord-message-channel    (if-let [connection (dm/start-connection! discord-api-token)]
                                                         connection
                                                         (throw (ex-info "Failed to connect to Discord message channel" {})))
                           :bot {
                             :ns                       bot-ns-name
                             :ns-sym                   (symbol bot-ns-name)
                             :intents                  bot-intents
                             :name                     (if-let [bot-name (get-in raw-config [:bot :name])]
                                                         bot-name
                                                         (first (s/split bot-ns-name #"\.+")))
                             :logo                     (if-let [bot-logo (get-in raw-config [:bot :logo])]
                                                         bot-logo
                                                         "https://cdn.jsdelivr.net/gh/IGJoshua/discljord/img/icon.png")
                             :colour                   (if-let [bot-colour (get-in raw-config [:bot :colour])]
                                                         bot-colour
                                                         9215480)
                             :http-status-port         (if-let [http-status-port (u/parse-int (get-in raw-config [:bot :http-status-port]))]
                                                         http-status-port
                                                         8080)
                           }
                         }))
          :stop (async/close!        (:discord-event-channel      config))
                (dc/disconnect-bot!  (:discord-connection-channel config))
                (dm/stop-connection! (:discord-message-channel    config)))

(declare  build-info)
(defstate build-info
          :start (when-let [build-info-resource (io/resource "build-info.edn")]
                   (when-let [raw-build-info (edn/read-string (slurp build-info-resource))]
                     (let [repo (s/replace (s/trim (get raw-build-info :repo "")) ".git" "")
                           sha  (s/trim (get raw-build-info :sha  ""))
                           tag  (s/trim (get raw-build-info :tag  ""))
                           date (:date raw-build-info)]
                       (merge (when-not (s/blank? repo) {:repo repo})
                              (when-not (s/blank? sha)  {:sha  sha})
                              (when-not (s/blank? tag)  {:tag  tag})
                              (when (and (not (s/blank? repo)) (or (not (s/blank? tag)) (not (s/blank? sha)))) {:build-url (str repo "/tree/" (if (s/blank? tag) sha tag))})
                              (when date {:build-date (tm/instant date)}))))))

(defn set-log-level!
  "Sets the log level (which can be a string or a keyword) of the bot, for the given logger aka 'package' (a String, use 'ROOT' for the root logger)."
  [level ^String logger-name]
  (when (and level logger-name)
    (let [logger    ^ch.qos.logback.classic.Logger (org.slf4j.LoggerFactory/getLogger logger-name)                       ; This will always return a Logger object, even if it isn't used
          level-obj                                (ch.qos.logback.classic.Level/toLevel (s/upper-case (name level)))]   ; Note: this code defaults to DEBUG if the given level string isn't valid
      (.setLevel logger level-obj))))

(defn reset-logging!
  "Resets all log levels to their configured defaults."
  []
  (let [lc ^ch.qos.logback.classic.LoggerContext (org.slf4j.LoggerFactory/getILoggerFactory)
        ci (ch.qos.logback.classic.util.ContextInitializer. lc)]
    (.reset lc)
    (.autoConfig ci)))
