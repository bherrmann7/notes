(ns notes.welcome
  (:require [clojure.java.io]
            [clojure.java.shell :as shell]
            [hiccup.core]
            )
  (:import org.pegdown.PegDownProcessor))

(def notes-dir)

(defn notes-dir-set [dir]
  (def notes-dir dir)
  )

(defn markup [text]
  (clojure.string/replace (.markdownToHtml (PegDownProcessor.) (if text text ""))
                          #"(?:[A-Z][a-z]+){2,}" "<a href=\"/$0\">$0</a>"))
(defn fetch-file [file]
  (let [
        env-dir (System/getenv "NOTES_GIT_DIR")
        base-dir (new java.io.File (if notes-dir notes-dir (if env-dir env-dir "wiki/")))
        pageFile (new java.io.File base-dir file)]
    (if (.exists pageFile) (do
                             (println (str "slurping " pageFile))
                             (slurp pageFile))
                           (str "\nNo file found on disk.  Looked at: " pageFile)))
  )

(defn file-to-wiki [file]
  (let [m (.getName file)
        no-wd (.replaceAll m ".wd" "")
        ]
    (str  no-wd "<br>")
    )
  )

(defn fetch-dir []
  (let [
        env-dir (System/getenv "NOTES_GIT_DIR")
        base-dir (new java.io.File (if notes-dir notes-dir (if env-dir env-dir "wiki/")))
        ]
    ; [example link](http://example.com/)
    (clojure.string/join (map file-to-wiki (.listFiles base-dir)))
    ))

(defn fetch-content [title]
  (if (= "Directory" title)
    (fetch-dir)
    (fetch-file (str title ".wd"))))

(defn save-content [title body]
  (let [env-dir (System/getenv "NOTES_GIT_DIR")
        base-dir (new java.io.File (if notes-dir notes-dir (if env-dir env-dir "wiki/")))
        disk-file (str title ".wd")
        pageFile (new java.io.File base-dir disk-file)
        ]
    (if-not (.exists base-dir) (.mkdirs base-dir))
    (println (str "spitting " pageFile))
    (spit pageFile body)
    ))

(defn mk-link [request & more]
  (clojure.string/join "" (cons (:context-path request) more))
  )

(defn layout [request title body]
  (hiccup.core/html
    [:html [:head [:link {:href "http://netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/css/bootstrap-combined.min.css" :rel "stylesheet" :media "screen"}]
            [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
            "<style>
  body {
  padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
  }
  </style>"
            ] [:body [:script {:src "http://code.jquery.com/jquery.js"}]
               [:script {:src "http://netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/js/bootstrap.min.js"}]

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
               <li ><a href='" (mk-link request "/Directory") "'>Directory</a></li>
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


(defn about-page [request]
  {:status 200 :body (layout request "About" (str "<div class='container'><div class='row'><div class='span12'>A simple wiki like system.   A mashup of Pedestal.io, Clojure.io, Git, Markdown, Wiki</div></div>
  <div class='row'><div class='span12'>Created by Bob Herrmann " "</div></div></div>"))})

(defn view-page [request]
  (let [title (:title (:path-params request))
        body (markup (fetch-content title))]
    {:status 200 :body (layout request title
                               (hiccup.core/html [:div.container [:div.row [:div.span12 [:a.pull-right.btn.btn-primary.btn-mini {"href" (mk-link request (clojure.string/join ["/_edit/" title]))} "Edit"]
                                                                            [:h1 title]
                                                                            ]
                                                                  ]
                                                  [:div.row [:div.span12 body]]
                                                  ]))}))


(defn edit-page [request]
  (let [title (:title (:path-params request))]
    {:status 200 :body (layout request "Welcome"
                               (hiccup.core/html [:div.container [:form {:method "POST" :action (mk-link request "/_save")}
                                                                  [:div.row [:div.span12 [:a.pull-right.btn.btn-mini {"href" (mk-link request (clojure.string/join ["/" title]))} "Cancel"]
                                                                             "&nbsp;&nbsp;"
                                                                             [:button.pull-right.btn.btn-primary.btn-mini {"href" (clojure.string/join ["/_edit/" title])} "Save"]]]
                                                                  [:div.row [:div.span12 [:input {:type "textfield" :name "title" :value title}]
                                                                             ]]
                                                                  [:br]
                                                                  [:div.row [:div.span12 [:textarea {:name "body" :rows 30 :style "width: 100%"} (fetch-content title)]]]]]))}))

(defn save-page [request]
  (let [title (get (:form-params request) "title")
        body (get (:form-params request) "body")]
    (save-content title body)
    {:status 302 :headers {"Location" (mk-link request "/" title)}}
    ))


