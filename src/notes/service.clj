(ns notes.service
  (:require [io.pedestal.service.http :as bootstrap]
            [io.pedestal.service.http.route :as route]
            [io.pedestal.service.http.body-params :as body-params]
            [io.pedestal.service.http.ring-middlewares :as middlewares]
            [io.pedestal.service.http.route.definition :refer [defroutes]]
            [ring.util.response :as ring-resp]
            [notes.welcome]
            [notes.index]
            [notes.help]
            [notes.file]

            ))

(defroutes routes
  [[["/" {:get notes.welcome/welcome}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body  (middlewares/multipart-params)  ]
     ["/_about" {:get notes.welcome/about-page}]
     ["/_edit/:title" {:get notes.welcome/edit-page}]
     ["/_print/:title" {:get notes.welcome/print-page}]
     ["/_delete/:title" {:get notes.welcome/delete-page}]
     ["/_save" {:post notes.welcome/save-page}]
     ["/_basic" {:get notes.help/basic-page}]
     ["/_syntax" {:get notes.help/syntax-page}]
     ["/_index" {:get notes.index/index}]
     ["/_new" {:get notes.welcome/new-page}]
     ["/:title" {:get notes.welcome/view-page}]
     ["/_upload" {:post notes.file/upload-file} ]
     ["/_res/:file" {:get notes.file/view-file}]
     ]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by notes.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::boostrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 7777})
