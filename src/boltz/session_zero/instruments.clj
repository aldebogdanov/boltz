(ns boltz.session-zero.instruments
  (:require [overtone.live :refer :all]
            [boltz.session-zero.general :refer [synth-group]]
            [boltz.session-zero.effects :refer [kick-rev-bus]]))

;; -- Main Kick --
(defsynth main-kick [amp 0.8 decay 0.3 freq 80 out-bus 0 pan 0]
  (let [env (env-gen (perc 0.01 decay) :action FREE)
        freq-env (env-gen (perc 0.001 decay) :level-scale freq)
        src1 (square:ar (+ freq freq-env))
        src2 (sin-osc:ar (+ freq freq-env))
        sig (* (+ src1 src2) env amp)
        filtered (lpf sig 2000)]
    (out out-bus (pan2 filtered pan))))


;; -- Main Bass --
(defsynth bass [amp 0.8 freq 55 out-bus 0 decay 0.1 pan 0 cutoff 800]
  (let [saw-env (env-gen (perc 0.01 (* 2 decay)) :action FREE)
        saw-src (saw:ar (+ freq (* 0.001 freq)))
        sin-env (env-gen (perc 0.01 (* 3 decay)) :action FREE)
        sin-src (sin-osc-fb (- freq (* 0.001 freq)))
        sig (+ (* sin-src sin-env amp) (* saw-src saw-env amp))
        filtered (lpf sig cutoff)]
    (out out-bus (pan2 filtered pan))))

(comment
  (bass :cutoff 2000)
  
  (main-kick [:tail synth-group] :freq 45)
  (main-kick [:tail synth-group] :out-bus kick-rev-bus :freq 45)

  (bass :freq 110 :cutoff 1700 :decay 0.3)
  
  (stop))
