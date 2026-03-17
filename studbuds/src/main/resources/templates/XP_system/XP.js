var CurrXp = 0;
let CurrentLvl = 1;
let XpToNextLvl = 100;
let totalXP = 0;


function give_XP(amount) {
    CurrXp += amount;
    totalXP += amount; 
    change_Percent();
    //change visuals 
    document.getElementById("myXP").innerHTML = CurrXp + "/" + XpToNextLvl;
    if (CurrXp >= XpToNextLvl){
        levelUp();
    }


}

function change_Percent() {
    let barPercent = (CurrXp / XpToNextLvl) * 100;
    document.querySelector(".XpBackgroundReactive").style.width = barPercent + "%";
}

function levelUp(){
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

    }
    change_Percent();
    document.getElementById("myXP").innerHTML = CurrXp + "/" + XpToNextLvl;
    document.getElementById("left").innerHTML = CurrentLvl;
    document.getElementById("right").innerHTML = CurrentLvl + 1;
}

function achievement_Detector(){
    if (CurrentLvl % 5 == 0){
        alert("ALERT! NEW ACHIEVEMENT UNLOCKED!!!");
    }
}
