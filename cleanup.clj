;; TODO: need to review this script and refactor some parts
(ns Scripts.cleanup
  (:require [clojure.java.shell :refer [sh]]
            [clj-time.format :as fmt]
            [clj-time.core :as time]))

(def interval 1800)
(def directory "./timestamp.log")
(def archive "timestamp.log")

(defn current-time-provider []
  (str "[" (-> (time/now) (fmt/format "%d/%m/%Y %H:%M:%S")) "]"))

(defn clean-audit []
  (sh "find" "path_para_remoção_ou_arquivos"))

(defn log-message [message]
  (let [timestamp (current-time-provider)]
    (spit directory (str timestamp " - " message "\n") :append true)))

(defn cleanup []
  (println "Cleaning up and exiting...")
  (log-message "Script terminated.")
  (System/exit 0))

(defn -main []
  #_{:clj-kondo/ignore [:missing-else-branch]}
  (if (not (.isDirectory (java.io.File. directory)))
    (do
      (println "Error: Log directory not found.")
      (System/exit 1)))

  #_{:clj-kondo/ignore [:missing-else-branch]}
  (if (not (.canWrite (java.io.File. directory)))
    (do
      (println "Error: Log file is not writable.")
      (System/exit 1)))

  (log-message "Script started")

  (try
    (loop []
      (println "Starting cleaning archives...")
      (clean-audit)
      (let [exit-code (.. (sh "echo" "$?") :exitValue)]
        (if (= exit-code 0)
          (log-message "Logs cleaned up successfully")
          (log-message "Logs cleanup failed")))
      (println (str "Sleeping for " interval " seconds..."))
      (Thread/sleep (* interval 1000))
      (recur))

    (catch Exception e
      (println "An error occurred:" (.getMessage e))
      (cleanup))))
