;
; Copyright © 2022 Peter Monks
;
; This Source Code Form is subject to the terms of the Mozilla Public
; License, v. 2.0. If a copy of the MPL was not distributed with this
; file, You can obtain one at https://mozilla.org/MPL/2.0/.
;
; SPDX-License-Identifier: MPL-2.0
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
