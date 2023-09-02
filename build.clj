(ns Scripts.build
  (:import [java.lang Thread]))

(defn build-application [app & build-params]
  (println (str "Building application " app))
  (let [build-command (concat ["sh" "-c" (str "cd " app " && gradle build")] build-params)
        process (.exec (Runtime/getRuntime) (into-array build-command))
        exit-code (.waitFor process)]
    (if (= exit-code 0)
      (println (str "Application " app " finished building!"))
      (println (str "Failed to build application " app " with exit code " exit-code)))))

(defn docker-compose-up [& compose-params]
  (println "Running containers!")
  (let [compose-command (concat ["docker-compose" "up" "--build" "-d"] compose-params)
        process (.exec (Runtime/getRuntime) (into-array compose-command))
        exit-code (.waitFor process)]
    (if (= exit-code 0)
      (println "Pipeline finished!")
      (println (str "Failed to run containers with exit code " exit-code)))))

(defn build-all-applications [& apps]
  (println "Starting to build applications!")
  (doseq [app apps]
    (let [thread (Thread. #(apply build-application app))]
      (.start thread))))

(defn remove-remaining-containers [& compose-params]
  (println "Removing all containers.")
  (let [compose-command (concat ["docker-compose" "down"] compose-params)
        process (.exec (Runtime/getRuntime) (into-array compose-command))
        exit-code (.waitFor process)]
    (if (= exit-code 0)
      (println "All containers removed.")
      (println (str "Failed to remove containers with exit code " exit-code)))))

(defn -main [& args]
  (let [apps ["my-app/myapplication1" ; Pode incluir quantos aplicativos desejar
              "my-app/myapplication2"
              "my-app/myapplication3"]
        build-params ["--param1" "--param2"] ; Parâmetros de construção
        compose-params ["--compose-param1" "--compose-param2"]] ; Parâmetros do Docker Compose

    (println "Pipeline started!")
    (apply build-all-applications apps)
    (Thread/sleep 1000) ; Espera um tempo para as threads terminarem
    (remove-remaining-containers compose-params)
    (Thread. (apply docker-compose-up compose-params))))
