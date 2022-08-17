package org.tyoda.wurm.PiggyBank.actions;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.tyoda.wurm.PiggyBank.PiggyBank;

import java.util.Collections;
import java.util.List;

public class BreakPiggyBankAction implements ModAction, BehaviourProvider, ActionPerformer {
    public final ActionEntry actionEntry;
    public final short actionId;
    public BreakPiggyBankAction(){
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(this.actionId, "Break the bank",
                "breaking the bank", new int[]{Actions.ACTION_TYPE_MAYBE_USE_ACTIVE_ITEM,
                                                    Actions.ACTION_TYPE_IGNORERANGE});
        ModActions.registerAction(actionEntry);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int tilex, int tiley, boolean onSurface, int tile) {
        PiggyBank pB = PiggyBank.getInstance();
        if(object.getTemplateId() != pB.getPiggyBankTemplateId()
                || !pB.breaksOnTile(Tiles.decodeType(tile)))
            return null;
        return Collections.singletonList(actionEntry);
    }

    public List<ActionEntry> getBehavioursFor(Creature performer, Item object, int tilex, int tiley, boolean onSurface, int tile, int dir) {
        return getBehavioursFor(performer, object, tilex, tiley, onSurface, tile);
    }

    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Item target) {
        PiggyBank pB = PiggyBank.getInstance();
        if(subject.getTemplateId() != pB.getPiggyBankTemplateId()
                || !pB.breaksOnItem(target.getTemplateId()))
            return null;
        return Collections.singletonList(actionEntry);
    }

    public boolean action(Action action, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, int dir, short num, float counter) {
        return this.action(action, performer, source, tilex, tiley, onSurface, heightOffset, tile, num, counter);
    }

    public boolean action(Action action, Creature performer, Item source, int tilex, int tiley, boolean onSurface, int heightOffset, int tile, short num, float counter) {
        PiggyBank pB = PiggyBank.getInstance();
        if(source.getTemplateId() != pB.getPiggyBankTemplateId()
                || !pB.breaksOnTile(Tiles.decodeType(tile)))
            return true;
        return PiggyBank.breakPiggy(performer, source, tilex, tiley, null);
    }

    public boolean action(Action action, Creature performer, Item subject, Item target, short num, float counter) {
        PiggyBank pB = PiggyBank.getInstance();
        if(subject.getTemplateId() != pB.getPiggyBankTemplateId()
                || !pB.breaksOnItem(target.getTemplateId())
                || !target.isOnSurface())
            return true;
        return PiggyBank.breakPiggy(performer, subject, target.getTileX(), target.getTileY(), target);
    }

    @Override
    public short getActionId() {
        return this.actionId;
    }
}
