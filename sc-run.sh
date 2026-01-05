#!/usr/bin/env bash

/Applications/SuperCollider.app/Contents/Resources/scsynth \
    -u 34168 \     # <udp-port-number>
    -b 1024 \      # <number-of-sample-buffers>        1024
    -z 64 \        # <block-size>  		       64
    -m 262144 \    # <real-time-memory-size>           8192
    -d 1024 \      # <max-number-of-synth-defs>        1024
    -V 0 \         # <verbosity>                       0
    -n 1024 \      # <max-number-of-nodes>             1024
    -r 64 \        # <number-of-random-seeds>          64
    -l 64 \        # <max-logins>                      64
    -D 0 \         # <load synthdefs? 1 or 0>          0
    -o 8 \         # <number-of-output-bus-chnnels>    8 
    -a 512 \       # <number-of-audio-bus-channels>    1024
    -R 0 \         # <publish to Rendezvous? 1 or 0>   0
    -c 4096 \      # <number-of-control-bus-channels>  16384
    -i 8 \         # <number-of-input-bus-channels>    8
    -w 64          # <number-of-wire-buffers>          64
