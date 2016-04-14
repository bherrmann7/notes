(ns notes.welcome
  (:require [clojure.java.io]
            [clojure.java.shell :as shell]
            [hiccup.core]
            )
  (:import org.pegdown.PegDownProcessor))
(require 'clojure.string)
(require 'clojure.java.io)

(def not-nil? (complement nil?))

(defn is-dir [f]
  (.exists (new java.io.File f))
  )

(defn choose-notes-dir []
  (if (not-nil? (System/getenv "NOTES_GIT_DIR"))
    (System/getenv "NOTES_GIT_DIR")
    (if (is-dir "/home/bob/notes-wiki")
      "/home/bob/notes-wiki"
      (if (is-dir "/Users/bob/notes-wiki")
        "/Users/bob/notes-wiki"
        (throw (new RuntimeException "Can not find notes-dir"))
        ))))

(def notes-dir-active (atom nil))

(defn get-notes-dir []
  (if (nil? @notes-dir-active)
    (reset! notes-dir-active (choose-notes-dir)))
  @notes-dir-active
  )

(defn markup [request text]
  (clojure.string/replace (.markdownToHtml (PegDownProcessor.) (if text text ""))
                          #"(?:[A-Z][a-z]+){2,}" (str "<a href=\"" (:context-path request) "/$0\">$0</a>")))
(defn fetch-file [file]
  (let [
        pageFile (new java.io.File (get-notes-dir) file)]
    (if (.exists pageFile) (do
                             (println (str "slurping " pageFile))
                             (slurp pageFile))
                           (str "\nNo file found on disk.  Looked at: " pageFile)))
  )

(defn fetch-content [title]
  (fetch-file (str title ".wd")))

(defn save-content [title body]
  (let [disk-file (str title ".wd")
        pageFile (new java.io.File (get-notes-dir) disk-file)
        ]
    (spit pageFile body)
    ))

(defn mk-link [request & more]
  (clojure.string/join "" (cons (:context-path request) more))
  )

(def resources-remote {
                       :bootstrap-css "http://netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap-combined.min.css"
                       :bootstrap-js  "http://netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/js/bootstrap.min.js"
                       :jsquery       "http://code.jquery.com/jquery.js"
                       })


(def resources-local {
                      :bootstrap-css "/bootstrap-combined.min.css"
                      :bootstrap-js  "/bootstrap.min.js"
                      :jsquery       "/jquery.js"
                      })

(def resources-use resources-local)

(defn layout [request title body]
  (hiccup.core/html
    [:html [:head
            [:link {:href (mk-link request (:bootstrap-css resources-use)) :rel "stylesheet" :media "screen"}]
            [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
            "<style>
  body {
  padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
  }
  </style>"
            ] [:body [:script {:src (mk-link request (:jquery resources-use))}]
               [:script {:src (:bootstrap-js (mk-link request resources-use))}]

               "<div class='navbar navbar-inverse navbar-fixed-top'>
               <div class='navbar-inner'>
               <div class='container'>
               <button type='button' class='btn btn-navbar' data-toggle='collapse' data-target='.nav-collapse'>
               <span class='icon-bar'></span>
               <span class='icon-bar'></span>
               <span class='icon-bar'></span>
               </button>
               <a class='brand' href='" (mk-link request "/") "'>Notes</a>
               <div class='nav-collapse collapse'>
               <ul class='nav'>
               <li class='active'><a href='" (mk-link request "/") "'>Home</a></li>
               <li ><a href='" (mk-link request "/_index") "'>Directory</a></li>
               </ul>
               <ul class='nav pull-right'>
                <li class='pull-right'><a href='" (mk-link request "/_basic") "'>Basic</a></li>
                <li class='pull-right'><a href='" (mk-link request "/_syntax") "'>Syntax</a></li>
                <li class='pull-right'><a href='" (mk-link request "/_about") "'>About</a></li>
               </ul>
               </div><!--/.nav-collapse -->
               </div>
               </div>
               </div>"
               body]]
    ))


(defn welcome [request]
  (clojure.pprint/pprint request)
  {:status 302 :headers {"Location" (mk-link request "/WelcomeDefault")}})


(defn cal-in-day [day]
  (apply + (map #(Integer/parseInt %) (map #(second %1) (re-seq #"(?m)\s(\d+)\scal" day)))))

(defn cal-day [day]
  (let [lines (clojure.string/split day #"\n")]
    (if (empty? day) ""
                     (str "## " (first lines) "Calories Total: " (cal-in-day day) "\n"
                          (clojure.string/join "\n" (rest lines))))))

(defn cal-count [text]
  (let [days (clojure.string/split text #"(?m)^##\s")]
    (clojure.string/join "\n" (map cal-day days))
    ))

(defn about-page [request]
  {:status 200 :body (layout request "About" (str "<div class='container'><div class='row'><div class='span12'>A simple wiki like system.   A mashup of Pedestal.io, Clojure.io, Git, Markdown, Wiki</div></div>
  <div class='row'><div class='span12'>Created by Bob Herrmann " "</div></div></div>"))})

(defn view-page [request]
  (let [title (:title (:path-params request))
        content (fetch-content title)
        raw-body (if (= title "Foodage") (cal-count content) content)
        body (markup request raw-body)
        ]
    {:status 200
     :body   (layout request title
                     (hiccup.core/html
                       [:div.container
                        [:div.row [:div.span12
                                   [:div.pull-right
                                    [:a.btn.btn-primary.btn-mini {"href" (mk-link request (clojure.string/join ["/_edit/" title]))} "Edit"]
                                    "&nbsp;&nbsp;&nbsp;"
                                    [:a.btn.btn-primary.btn-mini {"href" (mk-link request (clojure.string/join ["/_new"]))} "New"]
                                    ]
                                   [:h1 title]
                                   ]
                         ]
                        [:div.row [:div.span12 body]]
                        ]))}))

(defn edit-basics [request title body]
  {:status 200 :body (layout
                       request "Welcome"
                       (hiccup.core/html
                         [:div.container
                          [:form {:method "POST" :action (mk-link request "/_save")}
                           [:div.row [:div.span12
                                      [:div.pull-right
                                       [:a.btn.btn-mini.btn-danger {"href" (mk-link request (clojure.string/join ["/_delete/" title]))} "Delete"]
                                       "&nbsp;&nbsp;&nbsp;&nbsp;"
                                       [:a.btn.btn-mini {"href" (mk-link request (clojure.string/join ["/" title]))} "Cancel"]
                                       "&nbsp;&nbsp;"
                                       [:button.btn.btn-primary.btn-mini {"href" (clojure.string/join ["/_edit/" title])} "Save"]]]
                            ]
                           [:div.row [:div.span12 [:input {:type "textfield" :name "title" :value title}]
                                      ]]
                           [:br]
                           [:div.row [:div.span12 [:textarea {:name "body" :rows 30 :style "width: 100%"} body]]]]]))})


(defn edit-page [request]
  (let [title (:title (:path-params request))
        body (fetch-content title)]
    (edit-basics request title body)))


(defn save-page [request]
  (let [title (get (:form-params request) "title")
        body (get (:form-params request) "body")]
    (save-content title body)
    {:status 302 :headers {"Location" (mk-link request "/" title)}}
    ))

(defn delete-page [request]
  (let [title (:title (:path-params request))
        pageFile (new java.io.File (get-notes-dir) (str title ".wd"))]
    (.delete pageFile)
    ; Where do we go after delete?  Directory?
    {:status 302 :headers {"Location" (mk-link request "/_index?" (System/currentTimeMillis) )}})
  )


(defn new-page [request]
  (edit-basics request "WikiPageTitle" "BODY")
  )

(defn serv-resource [resource]

  )