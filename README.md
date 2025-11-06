# KIWI
KIWI is a custom VCS that provides less clutter and easy and interactive + colorful ( easy to read interface )

## SETUP WINDOWS 
```
git clone git@github.com:me-is-mukul/KIWI.git
cd KIWI
javac -d . src/*.java errors/*.java utils/*.java
setx PATH "%PATH%;%cd%"
```


## SETUP LINUX 
```
git clone git@github.com:me-is-mukul/KIWI.git
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
# YOUR KIWI SETUP IS DONE... GO TO ANY REPO AND TEST THE CUSTOM VCS ^3^