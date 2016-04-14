(ns notes.file
  (:import (java.io FileInputStream FileOutputStream File)))

(defn file-path [path & [filename]]
  (java.net.URLDecoder/decode
    (str path File/separator filename)
    "utf-8"))

; http://www.luminusweb.net/docs/routes.md
(defn upload-file
  "uploads a file to the target folder
   when :create-path? flag is set to true then the target path will be created"
  [path {:keys [tempfile size filename]}]
  (try
    (with-open [in (new FileInputStream tempfile)
                out (new FileOutputStream (file-path path filename))]
      (let [source (.getChannel in)
            dest   (.getChannel out)]
        (.transferFrom dest source 0 (.size source))
        (.flush out)))))


(defn view-file [request]
  (let [filename (:file (:path-params request))]
    {:status 200
     :headers {"Content-Type" "image/jpeg"}
     :body (FileInputStream. (str (notes.welcome/get-notes-dir) "/_res/" filename ))}))
