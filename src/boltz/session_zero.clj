(ns boltz.session-zero
  (:require [overtone.live :refer :all]
            [hyperfiddle.rcf]
            [boltz.session-zero.general :refer [synth-group]]
            [boltz.session-zero.instruments :refer [main-kick bass]]
            [boltz.session-zero.effects :refer [kick-rev-bus]]
            [overtone.inst.drum :as d]))

(hyperfiddle.rcf/enable!)
(stop)

(def m (metronome 192))


(defn player [beat]
  ;; Schedule the next call for 1 beat later
  (apply-at (m (inc beat)) #'player [(inc beat)])

  ;; 1. Play Kick on every beat (4/4)
  (at (m beat) (main-kick [:tail synth-group] :out-bus kick-rev-bus :freq 55 :amp 0.85))

  ;; 2. Triplet Bass
  (doseq [i (range 3)]
    (let [b (+ beat (/ i 3))
          x (rand)]
      (at (m b) (bass [:tail synth-group] :freq 55 :out-bus 0 :cutoff (if (<= x 0.1) (+ 400 (* 200 x)) 500)))))

  ;; 3. Hats
  #_(doseq [i (range 4)]
    (let [b (+ beat (/ i 4))
          a (if (zero? b) 0.6 0.4)]
      (at (m b) (d/hat3 :amp a :t 0.02))))

  ;; 4. Odd beats
  #_(when (odd? beat)
    (at (m beat) (d/open-hat)))
)


(player (#'m))
(stop)

(comment

  (d/open-hat)
  
  (stop)
  (server-info)
  (boot-external-server)

  (kill-server)

  (node-tree)

  (defonce x 2)
  x
  
)
