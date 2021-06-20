# DVC plugin for REMLA

![Build](https://github.com/Huntervang/REMLA/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/17050-remla.svg)](https://plugins.jetbrains.com/plugin/17050-remla)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/17050-remla.svg)](https://plugins.jetbrains.com/plugin/17050-remla)

<!-- Plugin description -->
DVC plugin for REMLA project.</br>

This is a proof of concept plugin for a student project from TU Delft that allows people unfamiliar with
(or not liking) command line interfaces or the DVC CLI in particular. It allows you to set a remote storage
(local, Google Drive or SSH connection), add and push files and reproduce a pipeline from a DVC.yaml file with
real time visualisation in the pipeline graph.</br>

For questions and remarks please contact us at A.H.Reurink@student.tudelft.nl.
<!-- Plugin description end -->

## Installation

Download the [latest release](https://github.com/Huntervang/REMLA/releases/latest) or find the .zip file in the `build/distributions` folder after running `gradle buildPlugin`and install it using
<kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>



The source code can also be ran directly by executing `gradle runIde` which downloads and caches a clean Ide to run the plugin in.