package net.draycia.spells;

import java.util.ArrayList;

public class SpellBook {
    public ArrayList<Spell> spells;

    private int currentSpell = 0;

    public SpellBook(ArrayList<Spell> spells) {
        this.spells = spells;
    }

    public void addSpell(Spell spell) {
        if (!spells.contains(spell))
            spells.add(spell);
    }

    public void removeSpell(Spell spell) {
        if (spells.contains(spell))
            spells.remove(spell);
    }

    public Spell getCurrentSpell() {
        return spells.get(currentSpell);
    }

    public void nextSpell() {
        if (currentSpell < spells.size())
            currentSpell++;
        else
            currentSpell = 0;
    }

    public void previousSpell() {
        if (currentSpell == 0)
            currentSpell = spells.size() - 1;
        else
            currentSpell--;
    }

}
