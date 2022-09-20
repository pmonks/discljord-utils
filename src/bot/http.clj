;
; Copyright © 2022 Peter Monks
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

(ns bot.http
  (:require [clojure.tools.logging :as log]
            [mount.core            :as mnt :refer [defstate]]
            [java-time             :as tm]
            [org.httpkit.server    :as http]
            [bot.config            :as cfg]
            [discljord-utils.util  :as u]))

(defn- http-status-handler
  [_]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (str "<!DOCTYPE html>
<html>
  <head><title>" (get-in cfg/config [:bot :name]) "</title></head>
  <body>" (get-in cfg/config [:bot :name]) " Discord bot up for " (u/human-readable-date-diff cfg/boot-time (tm/instant)) ".</body>
</html>")})

(declare  http-status-handler-server)
(defstate http-status-handler-server
  :start (let [port (get-in cfg/config [:bot :http-status-port])]
           (log/info (str "Starting HTTP status server on port " port))
           (http/run-server http-status-handler {:port port :legacy-return-value? false}))
  :stop  (when-let [stopping (http/server-stop! http-status-handler-server)] @stopping))
