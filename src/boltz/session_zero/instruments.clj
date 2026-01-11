(ns boltz.session-zero.instruments
  (:require [overtone.live :refer :all]
            [boltz.session-zero.general :refer [synth-group fx-group]]
            [boltz.session-zero.effects :refer [kick-rev-bus]]))

;; -- Main Kick --
(defsynth main-kick [amp 0.8 decay 0.3 freq 80 out-bus 0 pan 0]
  (let [env (env-gen (perc 0.01 decay) :action FREE)
        freq-env (env-gen (perc 0.01 decay) :level-scale freq)
        src1 (square:ar (+ freq (* 0.85 freq-env)))
        src2 (square:ar (+ freq (* 1.15 freq-env)))
        sig (* (+ src1 src2) env amp)
        filtered (lpf sig 2000)]
    (out out-bus (pan2 filtered pan))))


;; -- Main Bass --
(defsynth bass [amp 0.8 freq 55 out-bus 0 decay 0.1 pan 0 cutoff 800]
  (let [detune 0.002
        saw-env (env-gen (perc 0.01 (* 2 decay)) :action FREE)
        saw-src (saw (+ freq (* detune freq)))
        sin-env (env-gen (perc 0.01 (* 3 decay)) :action FREE)
        sin-src (sin-osc-fb (- freq (* detune freq)))
        sig (+ (* sin-src sin-env amp) (* saw-src saw-env amp))
        filtered (lpf sig cutoff)]
    (out out-bus (pan2 filtered pan))))


(defsynth glitch-lfo [freq 0.5 out-bus 0]
  (let [sig (lin-lin (sin-osc:kr freq 0) -1 1 300 8000)]
    (out out-bus sig)))

(def glitch-cutoff-bus (control-bus))

(free-bus glitch-cutoff-bus)

(def glitch-lfo-node
  (glitch-lfo [:tail fx-group] :freq 0.05 :out-bus glitch-cutoff-bus))

(kill glitch-lfo-node)


(defsynth glitch [amp 0.8 freq 440 decay 0.2 out-bus 0 pan 0 cutoff-bus 999]
  (let [env (env-gen (perc 0.01 decay 0.6 -1) :action FREE)
        src (pink-noise)
        nz (white-noise:ar)
        sig (* (+ src nz) env amp)
        filtered (lpf sig (in:kr cutoff-bus))
        mtl (bpf filtered (t-rand 1000 8000 (impulse:kr 10)) 0.1)]
    (out out-bus (pan2 mtl pan))))


(glitch [:tail synth-group] :freq 110 :decay 0.5 :amp 0.8 :cutoff-bus glitch-cutoff-bus)


(let [dt (* (/ 60 210) 1000)]
  (after-delay 0 #(glitch :freq 55))
  (after-delay (* 1 (/ dt 4)) #(glitch :freq 55))
  (after-delay (* 2 (/ dt 4)) #(glitch :freq 220))
  (after-delay (* 3 (/ dt 4)) #(glitch :freq 110)))


(kill glitch)

(comment
  (def bm (bus-monitor glitch-cutoff-bus))
  
  (add-watch bm :glitch-monitor
             (fn [_ _ old new]
               (when (not= old new)
                 (println (format "Bus monitor 'glitch-cutoff-bus': %s" new)))))
  (remove-watch bm :glitch-monitor)
 

  
  (bass :cutoff 2000)
  
  (main-kick [:tail synth-group] :freq 45)
  (main-kick [:tail synth-group] :out-bus kick-rev-bus :freq 45)

  (bass :freq 110 :cutoff 1700 :decay 0.3)
  
  (stop))
