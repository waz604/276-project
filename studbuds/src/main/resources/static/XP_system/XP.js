var CurrXp = 0;
let CurrentLvl = 1;
let XpToNextLvl = 100;
let totalXP = 0;

const MAX_LEVEL = 20;

function give_XP(amount) {
    if (CurrentLvl >= MAX_LEVEL && CurrXp >= XpToNextLvl) {
        return; // Already at level cap, don't accept more XP
    }

    CurrXp += amount;
    totalXP += amount; 

    // Clamp XP at max level cap
    if (CurrentLvl >= MAX_LEVEL && CurrXp >= XpToNextLvl) {
        CurrXp = XpToNextLvl;
    }

    triggerLevelUpAnimation(amount);
    change_Percent();
    //change visuals 
    document.getElementById("myXP").innerHTML = CurrXp + "/" + XpToNextLvl;
    document.getElementById("totXP").innerHTML = totalXP;

    if (CurrXp >= XpToNextLvl){
        levelUp();
    }


}

function change_Percent() {
    let barPercent = Math.min((CurrXp / XpToNextLvl) * 100, 100);
    document.querySelector(".XpBackgroundReactive").style.width = barPercent + "%";
}

function levelUp(){

     if (CurrentLvl >= MAX_LEVEL - 1) {
        CurrXp = XpToNextLvl; 
        change_Percent();
        document.getElementById("myXP").innerHTML = "MAX";
        document.getElementById("right").innerHTML = "MAX";
        return;
    }

    if (CurrXp == XpToNextLvl){
        CurrentLvl += 1;
        CurrXp = 0;
        XpToNextLvl = CurrentLvl * 100;
       achievement_Detector();
    }

    else{
        while (CurrXp > XpToNextLvl){
            let leftover = CurrXp - XpToNextLvl;
            CurrentLvl += 1; 
            CurrXp = leftover;
            XpToNextLvl = CurrentLvl * 100;
           achievement_Detector();
        }

        if (CurrentLvl >= MAX_LEVEL && CurrXp >= XpToNextLvl) {
            CurrXp = XpToNextLvl;
        }
    }
    change_Percent();
    document.getElementById("left").innerHTML = CurrentLvl;
    
    if (CurrentLvl >= MAX_LEVEL) {
        document.getElementById("left").innerHTML = "MAX";
        document.getElementById("myXP").innerHTML = "MAX";
        document.getElementById("right").innerHTML = "MAX";
        document.querySelector(".XpBackgroundReactive").style.width = 100 + "%";

    } else {
        document.getElementById("myXP").innerHTML = CurrXp + "/" + XpToNextLvl;
        document.getElementById("right").innerHTML = CurrentLvl + 1;
    }

    triggerLevelUpAnimation();
}

function achievement_Detector(){
    if (CurrentLvl % 5 == 0){
        //replace alert with connection to achievement framework
         showAchievementBanner("Achievement Unlocked! Reached Level " + CurrentLvl + "!");
    }
}

//animations
function triggerXpGainAnimation(amount) {
    const bar = document.querySelector(".XpBackgroundReactive");
    bar.classList.remove("xp-flash");
    void bar.offsetWidth; // reflow to restart animation
    bar.classList.add("xp-flash");

    // Floating "+XP" text
    const floater = document.createElement("div");
    floater.className = "xp-floater";
    floater.textContent = "+" + amount + " XP";
    document.querySelector(".container").appendChild(floater);
    setTimeout(() => floater.remove(), 1000);
}

function triggerLevelUpAnimation() {
    const container = document.querySelector(".container");
    const banner = document.createElement("div");
    //banner.className = "level-up-banner";
   // banner.textContent = CurrentLvl >= MAX_LEVEL ? "MAX LEVEL!" : "LEVEL UP! → " + CurrentLvl;
    //container.appendChild(banner);
    //setTimeout(() => banner.remove(), 1800);
}

function showAchievementBanner(message) {
    const banner = document.createElement("div");
    banner.className = "achievement-banner";
    banner.textContent = "🏆 " + message;
    document.body.appendChild(banner);
    setTimeout(() => banner.remove(), 3000);
}
