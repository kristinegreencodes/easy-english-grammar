# Self-composed, royalty-free calm Japanese-fantasy loop (koto, shakuhachi-style flute, pad, chimes).
import numpy as np, wave, sys
SR = 32000
BPM = 64
BEAT = 60 / BPM
BARS = 24
LEN = int(BARS * 4 * BEAT * SR)
rng = np.random.default_rng(3)
out = np.zeros(LEN + SR * 4)

def hz(m): return 440 * 2 ** ((m - 69) / 12)

def add(sig, t):
    i = int(t * SR)
    out[i:i + len(sig)] += sig[: len(out) - i]

def koto(m, dur=2.5, vel=0.5):
    n = int(dur * SR); tt = np.arange(n) / SR
    f = hz(m) * (1 + 0.006 * np.exp(-tt * 25))  # tiny pitch drop after pluck
    s = np.zeros(n)
    for h in range(1, 9):
        amp = (1 / h) * (0.9 if h % 2 else 0.6)
        s += amp * np.sin(2 * np.pi * f * h * tt + h) * np.exp(-tt * (1.8 + h * 1.3))
    atk = np.minimum(1, tt / 0.004)
    return vel * s * atk * 0.35

def flute(m, dur, vel=0.22):
    n = int(dur * SR); tt = np.arange(n) / SR
    vib = 1 + 0.004 * np.sin(2 * np.pi * 5 * tt) * np.minimum(1, tt / 0.8)
    ph = 2 * np.pi * np.cumsum(hz(m) * vib) / SR
    s = np.sin(ph) + 0.25 * np.sin(2 * ph) + 0.08 * np.sin(3 * ph)
    breath = rng.standard_normal(n)
    breath = np.convolve(breath, np.ones(30) / 30, mode="same") * 0.35
    env = np.minimum(1, tt / 0.35) * np.minimum(1, (dur - tt) / 0.6)
    env = np.clip(env, 0, 1) ** 1.5
    return vel * (s + breath) * env * 0.5

def pad(ms, dur, vel=0.10):
    n = int(dur * SR); tt = np.arange(n) / SR
    s = np.zeros(n)
    for m in ms:
        for d in (-0.07, 0.0, 0.07):
            s += np.sin(2 * np.pi * hz(m + d) * tt + rng.uniform(0, 6))
    env = np.clip(np.minimum(tt / 1.5, (dur - tt) / 1.5), 0, 1)
    lfo = 0.8 + 0.2 * np.sin(2 * np.pi * 0.15 * tt)
    return vel * s / (3 * len(ms)) * env * lfo

def chime(m, vel=0.06):
    n = int(4 * SR); tt = np.arange(n) / SR
    s = np.sin(2 * np.pi * hz(m) * tt) + 0.4 * np.sin(2 * np.pi * hz(m) * 2.76 * tt) * np.exp(-tt * 3)
    return vel * s * np.exp(-tt * 1.2)

bar = 4 * BEAT
# Chords every 2 bars: D, Bb, F, A (pad voicings in MIDI)
chords = [[38, 45, 50, 53], [34, 41, 46, 50], [41, 45, 48, 53], [33, 40, 45, 52]]
arps = [[50, 57, 62, 65], [46, 53, 58, 62], [53, 57, 62, 65], [45, 52, 57, 62]]
for b in range(0, BARS, 2):
    c = (b // 2) % 4
    add(pad(chords[c], 2 * bar + 1.5), b * bar)
    # gentle koto arpeggio: up on beats 1-4 of first bar, sparse in second
    for k, m in enumerate(arps[c]):
        add(koto(m, 2.5, 0.35), b * bar + k * BEAT * 0.5)
    add(koto(arps[c][2] + 12, 2.5, 0.25), b * bar + bar + BEAT)

# Koto melody in D hirajoshi (D E F A Bb), (midi, start beat, length beats)
mel = [
    (74, 0, 2), (76, 2, 1), (77, 3, 1), (76, 4, 3), (74, 8, 2), (70, 10, 2), (69, 12, 4),
    (69, 16, 1), (70, 17, 1), (74, 18, 2), (77, 20, 2), (76, 22, 2), (74, 24, 6),
    (81, 32, 2), (77, 34, 2), (76, 36, 2), (74, 38, 2), (70, 40, 3), (69, 43, 1), (70, 44, 4),
    (74, 48, 1), (76, 49, 1), (77, 50, 2), (81, 52, 2), (77, 54, 2), (74, 56, 8),
]
for rep_start in (0, 48):  # melody plays twice, second time an octave lower in places
    for (m, s, l) in mel:
        if rep_start and s >= 32:
            continue
        mm = m if not rep_start else m - 12 if m > 76 else m
        add(koto(mm, max(1.6, l * BEAT + 1), 0.55), (rep_start + s) * BEAT)

# Shakuhachi-style flute answers in the second half
fl = [(69, 48, 4), (74, 52, 3), (72 - 2, 55, 1), (69, 56, 8), (77, 64, 4), (76, 68, 2), (74, 70, 2), (74, 72, 8), (69, 80, 6), (70, 86, 2), (74, 88, 8)]
for (m, s, l) in fl:
    add(flute(m, l * BEAT), s * BEAT)

# Wind chimes, sparse
for t in np.arange(0, BARS * bar, bar * 1.5):
    add(chime(rng.choice([86, 88, 89, 93, 94])), t + rng.uniform(0, 1))

# Reverb: convolve with decaying noise impulse, then wrap the tail for a seamless loop
ir_n = int(2.8 * SR); ti = np.arange(ir_n) / SR
ir = rng.standard_normal(ir_n) * np.exp(-ti * 2.2)
ir = np.convolve(ir, np.ones(8) / 8, mode="same")
ir[0] = 0
N = 1 << int(np.ceil(np.log2(len(out) + ir_n)))
wet = np.fft.irfft(np.fft.rfft(out, N) * np.fft.rfft(ir, N), N)[: len(out) + ir_n]
wet *= 0.25 / np.max(np.abs(wet))
mix = np.zeros(len(wet)); mix[: len(out)] += out
mix = 0.75 * mix / np.max(np.abs(mix)) + wet
loop = mix[:LEN].copy()
tail = mix[LEN:]
loop[: len(tail)] += tail
loop = np.tanh(loop * 1.1)
loop = loop / np.max(np.abs(loop)) * 0.8
pcm = (loop * 32767).astype(np.int16)
with wave.open(sys.argv[1], "wb") as w:
    w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes(pcm.tobytes())
print("seconds", LEN / SR)
