# inaudible-sound-data-transmission

Authors: João Victor Girard and Alan L.V. Guedes
TeleMídia - Puc Rio

## intro
This is a Readme file for the android app project AudioChecker, its transmission and 
iterations along the development.

The purpose of the app is to recognize a message encoded on high frequencies being 
transmited on the user surroundings so it can be received by the microfone and decoded 
on the device.

All frequencies are close or above the human hearing capacity. So it is inteded to the
hard or impossible to detect without equipments the signal being broadcasted.

The latest version includes syncronization, error correction and real time threshold
adaptation for the transmission. The transmission is currently done by an amplitude 
modulation and the bit rate is 17 bps.


## paper

An paper was published in WTIC workshop at WebMedia 2016

- [paper file](https://portaldeconteudo.sbc.org.br/index.php/webmedia_estendido/article/download/4059/3999)

```bibtex
@inproceedings{nunes2018,
  langid = {english},
  title = {Towards {{Data Transmission Through Inaudible Sound}} in {{Ginga}}-{{NCL}}},
  url = {https://portaldeconteudo.sbc.org.br/index.php/webmedia_estendido/article/download/4059/3999},
  doi = {https://doi.org/10.5753/webmedia.2018.4568},
  abstract = {In this paper, we report our efforts to add support for data transmission through inaudible sound to the Ginga-NCL Digital TV middleware. We present an algorithm for encoding a bitstream in an inaudible audio signal, and to do so reliably on consumergrade hardware. We also discuss two attempts to implement this algorithm in NCL, the language in which Ginga-NCL applications are written. The first attempt was to transmit prerecorded inaudible audio signals in a Ginga-NCL-compatible set-top-box. And the second attempt was to use NCLua to generate at runtime the inaudible audio signal. For the second attempt we extended NCL with a novel media object type, called SigGen, which can be used to generate arbitrary audio signals. In the paper, we describe in detail the implementation of SigGen and the result of these attempts.},
  booktitle = {Anais Do {{XXIII Simp\'osio Brasileiro}} de {{Sistemas Multim\'idia}} e {{Web}}: {{Workshops}} e {{P\^osteres}}},
  date = {2018},
  pages = {4},
  author = {Nunes, Jo\~ao Victor G. de S. and Guedes, \'Alan L. V. and Lima, Guilherme F. and Colcher, S\'ergio},
  note = {00000}
}
```
