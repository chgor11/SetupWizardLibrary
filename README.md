this is a fork from https://gitea.angry.im/PeterCxy/SetupWizardLibrary/




rm -rf /storage/emulated/0/Audiobooks/Shelter && cd /storage/emulated/0/Audiobooks && git clone 'https://github.com/chgor11/Shelter.git' && cd /storage/emulated/0/Audiobooks/Shelter && git submodule update --init --remote --merge libs/SetupWizardLibrary && git add libs/SetupWizardLibrary && git commit -m "Update SetupWizardLibrary to latest main" && git push && exit
