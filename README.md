
![Java](https://img.shields.io/badge/Java-21.0.8-orange)
![Platform](https://img.shields.io/badge/OS-Windows%20%7C%20Linux%20%7C%20macOS-blue)
![License](https://img.shields.io/badge/License-MIT-lightgrey)
![Version](https://img.shields.io/badge/Version-1.0.0-green)

<p align="center">
  <img src="logo.png" width="200" alt="KIWI Logo"/>
</p>

# 🥝 KIWI — A Minimal & Colorful Version Control System

**KIWI** is a custom-built **Version Control System (VCS)** that aims to provide a **cleaner, more colorful, and interactive** command-line experience.  
It’s lightweight, easy to use, and perfect for understanding how VCS tools like Git work under the hood — without all the clutter!

---

##  Features
- **Colorful Output** — Easy-to-read terminal interface with styled logs and commit history  
- **Lightweight Design** — No dependencies, built purely in Java  

- **Educational** — Ideal for learning VCS fundamentals like staging, committing, and logging  

- **Cross-Platform** — Works seamlessly on **Windows**, **Linux**, and **macOS**  

---

## Setup Instructions

###  **For Linux**
```bash
git clone https://github.com/me-is-mukul/KIWI.git
cd KIWI
javac -d . src/*.java errors/*.java utils/*.java
chmod +x kiwi
if [ -n "$ZSH_VERSION" ]; then
  echo "export PATH=\$PATH:$(pwd)" >> ~/.zshrc
  source ~/.zshrc
else
  echo "export PATH=\$PATH:$(pwd)" >> ~/.bashrc
  source ~/.bashrc
fi
```

###  **For Windows**
```bash
git clone https://github.com/me-is-mukul/KIWI.git
cd KIWI
javac -d . src/*.java errors/*.java utils/*.java
setx PATH "%PATH%;%cd%"
```

## USAGE SCREENSHOTS

### Initialising and Status
<p align="left">
  <img src="images/1.png" width="250" alt="KIWI Logo"/>
</p>

### Add before modi-fication
<p align="left">
  <img src="images/2.png" width="250" alt="KIWI Logo"/>
</p>

### Add after modi-fication
<p align="left">
  <img src="images/3.png" width="250" alt="KIWI Logo"/>
</p>

### Commit
<p align="left">
  <img src="images/4.png" width="250" alt="KIWI Logo"/>
</p>

### LOGS
<p align="left">
  <img src="images/5.png" width="250" alt="KIWI Logo"/>
</p>

### Collaborator 
GitHub: @SaraJain90