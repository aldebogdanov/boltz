(ns boltz.core
  (:require
   [overtone.music.rhythm :refer [metronome]]
   [overtone.studio.inst :refer [definst]]
   [overtone.music.drums :refer ]
   [overtone.sc.ugens :refer [env-gen]]
   [overtone.live :refer :all]))


(def metro (metronome 128))

;; A deep, distorted Kick
(definst industrial-kick [amp 0.8 decay 0.4 freq 60]
  (let [env (env-gen (perc 0.001 decay) :action FREE)
        f-env (env-gen (perc 0.001 0.1) :level-scale freq) ;; Pitch drop
        src (sin-osc (+ freq f-env))
        dist (tanh (* 6 src))] ;; Tanh distortion is essential for "Industrial"
    (* dist env amp)))

;; A metallic Hi-hat / Noise
(definst glitch-hat [amp 0.5 pan 0]
  (let [env (env-gen (perc 0.005 0.05) :action FREE)
        noise (white-noise)
        ;; Band Pass Filter with high resonance for "metallic" tone
        metal (bpf noise (t-rand 1000 8000 (impulse:kr 10)) 0.1)]
    (pan2 (* metal env amp) pan)))



(defn player [beat]
  (println beat)
  ;; Schedule the next call for 1 beat later
  (apply-at (metro (inc beat)) #'player [(inc beat)])

  ;; 1. Play Kick on every beat (4/4)
  (at (metro beat) (industrial-kick))

  ;; 2. Play Off-beat Hats (Techno logic: "and" check)
  ;; "odd?" means it plays on beats 1, 3, 5... (0-indexed)
  (if (odd? beat)
    (at (metro beat) (glitch-hat :amp 0.3)))

  ;; 3. Probabilistic Glitch (The "IDM" factor)
  ;; 30% chance to play a random glitch in between beats
  (if (< (rand) 0.3)
    (at (metro (+ 0.5 beat)) (glitch-hat :pan (-> (rand) (* 2) (- 1)) :amp 0.2))))
;;

(player (metro))
(stop)


(industrial-kick :out 0)
(glitch-hat)

