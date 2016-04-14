(ns notes.index
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as shell]
            [hiccup.core]
            [notes.welcome]
            )
  (:import org.pegdown.PegDownProcessor))


(defn file-to-wiki [file]
  (let [m (.getName file)
        no-wd (.replaceAll m ".wd" "")
        ]
      (if (= "res" m )
        ""
        (str no-wd "<br>")
    )
  ))

(defn fetch-dir [request]
    ; [example link](http://example.com/)
    (notes.welcome/markup request
                          (clojure.string/join (sort (map file-to-wiki (.listFiles (new java.io.File (notes.welcome/get-notes-dir))))))
    ))

(defn index [request]
  {:status 300 :body
           (notes.welcome/layout
               request "WikiPageTitle"
             (hiccup.core/html
               [:div.container
                [:div.row [:div.span12
                           [:div.pull-right
                            [:a.btn.btn-primary.btn-mini {"href" (notes.welcome/mk-link request (clojure.string/join ["/_new"]))} "New"]
                            ]]]

                (fetch-dir request)
                ]
               ))})
