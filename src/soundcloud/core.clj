(ns soundcloud.core
  (:require [cheshire.core :refer :all]
            [org.httpkit.client :as http]
            [org.httpkit.server :as serv])
  (:gen-class))

(def client-id "a454bbc3e7c73dd307130edc1e2bed00")

(defn mk-search-url [page q]
  (str "https://api.soundcloud.com/tracks.json?client_id=" client-id "&genres="
       q "&limit=200&offset=" (* page 200)))

(defn get-tracks [genre]
  (loop [page 0 c []]
    (println "page: " page)
    (let [new-tracks (-> page (mk-search-url genre) http/get deref
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
                  "'});
  });
</script></body></html>")})

(defn -main [& args]
  (let [tracks (shuffle (get-tracks "nature+sounds"))
        links-to-tracks (take 20 (map :permalink_url tracks))
        handler (partial app links-to-tracks)]
    (serv/run-server handler {:port 8080})))

(comment
  (def tracks (get-tracks "nature+sounds"))
  (def long-tracks (->> tracks (sort-by :duration) reverse))
  (def play-tracks (take 20 (map :permalink_url long-tracks)))
  (def handler (partial app play-tracks))
  (first play-tracks)

  (def tracks (take 20 (map :permalink_url (shuffle (get-tracks "nature+sounds")))))

  (def stop-server (serv/run-server handler {:port 8080}))

  )
