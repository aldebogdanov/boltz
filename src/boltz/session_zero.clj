(ns boltz.session-zero
  (:require [overtone.live :refer :all]
            [hyperfiddle.rcf]
            [boltz.session-zero.general :refer [synth-group]]
            [boltz.session-zero.instruments :refer [main-kick bass glitch glitch-cutoff-bus]]
            [boltz.session-zero.effects :refer [kick-rev-bus]]
            [overtone.inst.drum :as d]
            [overtone.at-at :as at]))

(hyperfiddle.rcf/enable!)

(stop)


(defonce !bpm (atom 190))

(def m (metronome @!bpm))


(defonce !player-running? (atom false))


(defonce !patterns (atom {}))


(defn player
  "Runs in parallel all pattern vars from `!patterns` atom"
  [beat]
  (->> @!patterns
       vals
       (pmap #(% beat))
       dorun))


(defn kick-4-4 [beat]
  (at (m beat) (main-kick [:tail synth-group] :out-bus kick-rev-bus :freq 55 :amp 0.8)))

(defn dub-kick [beat]
  (at (m beat) (d/dub-kick)))


(defn bass-triple [beat]
  (doseq [i (range 3)]
    (let [b (+ beat (/ i 3))]
      (at (m b) (bass [:tail synth-group] :freq 55 :out-bus 0 :cutoff 2200 :decay 0.06)))))

(defn hats-8 [beat]
  (doseq [i (range 2)]
    (let [b (+ beat (/ i 2))
          a (if (zero? i) 0.3 0.15)]
      (at (m b) (d/hat3 :amp a :t 0.02)))))



(defn glitchy-thing [beat]
  (doseq [i (range 4)]
    (let [b (+ beat (/ i 4))
          p? (not= 3 i)
          l? (= 2 i)]
      (when p?
        (at (m b) (glitch [:tail synth-group]
                          :cutoff-bus glitch-cutoff-bus
                          :out-bus kick-rev-bus
                          :freq 100
                          :decay (if l? 0.5 0.35)
                          :amp 1.0))))))


;; REPL-Safe Singleton Player
(def player-pool (at/mk-pool))

(defonce !scheduler-id (atom nil))  ; Track running scheduler

(defn start-player []
  (when @!scheduler-id
    (at/stop @!scheduler-id)
    (reset! !scheduler-id nil))
  
  (reset! !player-running? true)
  (reset! !scheduler-id
    (at/every (/ 60000 @!bpm)
              #(let [current-beat (m)]
                 (player current-beat))
              player-pool
              :desc "Session Zero player")))

(defn stop-player []
  (reset! !player-running? false)
  (when @!scheduler-id
    (at/stop @!scheduler-id)
    (reset! !scheduler-id nil)))


;; Live-coding helper: restart player with current code
(defn restart-player []
  (stop-player)
  (Thread/sleep 150)  ; Brief pause to ensure cleanup
  (start-player)
  (println "Player restarted at" @!bpm "BPM"))


;; Change BPM and automatically restart if playing
(defn set-bpm-and-restart [bpm]
  (let [was-playing @!player-running?]
    (when was-playing (stop-player))
    (m :bpm (reset! !bpm bpm))
    (when was-playing (start-player))
    (println "BPM set to" @!bpm (when was-playing "(player restarted)"))))


(defn pset! [& args]
  (let [pairs (partition 2 args)]
    (doseq [[k pattern-fn] pairs]
      (let [v (resolve pattern-fn)]
        (swap! !patterns assoc k v)
        (println (format "Pattern set: %s -> %s" k (symbol v)))))))


(defn pdrop! [& ks]
  (doseq [k ks]
    (swap! !patterns dissoc k)
    (println "Pattern dropped:" k)))


(comment

  (pset! :g 'glitchy-thing)
  (pdrop! :g)

  (pset! :k 'kick-4-4
         :b 'bass-triple
         :h 'hats-8)

  (pdrop! :k :h :b :g)

  (d/snare)
  (main-kick [:tail synth-group] :out-bus kick-rev-bus :freq 55)

  
  ;; ===== CONTROL =====
  (start-player)   ; Start with current code
  (stop-player)    ; Stop completely
  (restart-player) ; Stop and start with latest code
  
  ;; ===== TEMPO =====
  (set-bpm-and-restart 128)  ; Change BPM, restart if playing
  (set-bpm-and-restart 210)
  @!bpm  ; Check current BPM
  
  ;; ===== INSTRUMENTS =====
  (toggle-kick)
  (toggle-bass)
  (toggle-hats)
  
  ;; See current states:
  {:kick @!kick? :bass @!bass? :hats @!hats?}
  
  ;; ===== DEBUG =====
  (server-info)
  (node-tree)
  
  ;; ===== EXPERIMENT =====
  ;; Modify the player function above, then:
  (restart-player)  ; Instantly hear changes
  
  ;; Try new patterns in isolation:
  (do
    (toggle-hats)
    (at (m (inc (m))) #(d/hat3 :amp 0.4)))
  
  ;; ===== CLEANUP =====
  (stop-player)
  (group-free boltz.session-zero.general/fx-group)
  (kill-server)
  (boot-server)
  (stop)
)
