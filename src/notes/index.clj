(ns notes.index
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [hiccup.core]
            [notes.welcome]
            )
  (:import org.pegdown.PegDownProcessor))

(defn html-dir
  ( [dir] (html-dir (io/file dir) ""))
  ( [dir html-so-far]
    (let [dirs (filter #(complement (.isDirectory %)) (.listFiles dir) )
          ]
    (into [] (flatten [ :div.span1 "Files are "  (map  #(vector :br (.getName %)) dirs ) ]))
    )))

(defn index3 [request]
  {:status 300 :body "boo bo you" })

(defn index [request]
  {:status 300 :body (notes.welcome/layout request "tinker do"

                                           (hiccup.core/html [:div.container

                                                                         (html-dir (System/getenv "NOTES_GIT_DIR")) ]
                                                                          ))})
