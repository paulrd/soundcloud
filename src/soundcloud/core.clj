(ns soundcloud.core
  (:require [cheshire.core :refer :all]
            [org.httpkit.client :as http]
            [clj-http.client :as client]
            [org.httpkit.server :as serv])
  (:gen-class))

(def client-id "a454bbc3e7c73dd307130edc1e2bed00")

(defn mk-search-url [page q]
  (str "https://api.soundcloud.com/tracks.json?client_id=" client-id "&genres="
       q "&limit=200&offset=" (* page 200)))

(defn get-tracks [genre]
  (loop [page 0 c []]
    (println "page: " page)
    (let [new-tracks (-> page (mk-search-url genre) client/get #_http/get
                         :body (parse-string true))]
      (if (= (count new-tracks) 0)
        c
        (recur (inc page) (concat c new-tracks))))
    )
  )


(defn app [links-to-tracks req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (str  "<!DOCTYPE! html><html><head><script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js\"></script><script type=\"text/javascript\" src=\"https://stratus.soundcloud.com/stratus.js\"></script></head><body><script type=\"text/javascript\">
  $(document).ready(function(){
    $.stratus({links: '"
                  (reduce #(str %1 "," %2) links-to-tracks)
                  "', random: true, download: false, auto_play: true, buying: false, volume: 100});
  });
</script></body></html>")})

(defn -main [& args]
  (let [tracks (->> "nature+sounds" get-tracks shuffle (take 86))
        total-duration (/ (reduce #(+ %1 (:duration %2)) 0 tracks) 1000.0 60 60)
        _ (println "total duration:" (format "%.2f" total-duration) "hours")
        links-to-tracks (map :permalink_url tracks)
        handler (partial app links-to-tracks)]
    (serv/run-server handler {:port 8080})))

(comment
  (def a (-> 0 (mk-search-url "nature+sounds") (http/get {:insecure? true})))

  (def b (-> 0 (mk-search-url "nature+sounds") client/get))
  b
  a
  (mk-search-url 0 "nature+sounds")

  (def tracks (->> "nature+sounds" get-tracks shuffle (take 20)))
  (get-tracks "nature+sounds")
  (count tracks)
  (def total-duration
    (/ (reduce #(+ %1 (:duration %2)) 0 tracks) 1000.0 60 60))
  (def long-tracks (->> tracks (sort-by :duration) reverse))
  (def play-tracks (take 20 (map :permalink_url long-tracks)))
  (def handler (partial app play-tracks))
  (first play-tracks)

  (-main)
  (def tracks (take 20 (map :permalink_url (shuffle (get-tracks "nature+sounds")))))

  (def stop-server (serv/run-server handler {:port 8080}))
  (-main)
  )
