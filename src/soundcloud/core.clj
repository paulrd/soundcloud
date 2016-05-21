(ns soundcloud.core
  (:require [cheshire.core :refer :all]
            [org.httpkit.client :as http]
            [org.httpkit.server :as serv]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def client-id "a454bbc3e7c73dd307130edc1e2bed00")

(defn mk-request-url [name]
  (str "https://api.soundcloud.com/users/" name  "/tracks.json?client_id=" client-id))


(def req (http/get (mk-request-url "warp-records")))

(comment
  (-main)

  (mk-request-url "ninja-tune")
  req
  (def req2 (http/get (mk-request-url "warp-records")))
  @req2
  (:body @req2)
  (:error @req2)
  (def tracks (parse-string (:body @req)))
  tracks
  (count tracks)
  (keys (first tracks))
  (def tracks (parse-string (:body @req) true))
  tracks
  (:license (first tracks))
  )
