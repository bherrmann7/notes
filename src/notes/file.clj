(ns notes.file
  (:require
            [notes.utils :as nu])
  (:import (java.io FileInputStream FileOutputStream File)))

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

(defn file-path [path & [filename]]
  (java.net.URLDecoder/decode
    (str path File/separator filename)
    "utf-8"))

(defn get-notes-dir []
  (if (nil? @notes-dir-active)
    (do
      (reset! notes-dir-active (choose-notes-dir))
      (.mkdirs (new java.io.File ^String (file-path @notes-dir-active "_res" )))
      )
    )
  @notes-dir-active
  )

; http://www.luminusweb.net/docs/routes.md
(defn upload-file
  "uploads a file to the target folder
   when :create-path? flag is set to true then the target path will be created"
  [request]
  (let [mp (get (:multipart-params request) "file")
        {:keys [tempfile size filename]} mp
        origin-title (get (:multipart-params request) "origin-title")]
  (try
    (with-open [in (new FileInputStream tempfile)
                out (new FileOutputStream (new java.io.File (str (get-notes-dir) "/_res/" filename)))]
      (let [source (.getChannel in)
            dest   (.getChannel out)]
        (.transferFrom dest source 0 (.size source))
        (.flush out))))
  {:status 302 :headers {"Location" (nu/mk-link request "/_edit/" origin-title)}}
  )
  )



(defn view-file [request]
  (let [filename (:file (:path-params request))]
    {:status 200
     :headers {"Content-Type" "image/jpeg"}
     :body (FileInputStream. (file-path (str (get-notes-dir) "/_res") filename ))}))
