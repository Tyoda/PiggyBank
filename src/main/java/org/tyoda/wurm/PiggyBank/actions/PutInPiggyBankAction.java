package org.tyoda.wurm.PiggyBank.actions;

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

public class PutInPiggyBankAction implements ModAction, BehaviourProvider, ActionPerformer {
    private final ActionEntry actionEntry;
    private final short actionId;
    public PutInPiggyBankAction(){
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(this.actionId, "Deposit",
                "depositing", new int[]{Actions.ACTION_TYPE_MAYBE_USE_ACTIVE_ITEM});
        ModActions.registerAction(actionEntry);
    }

    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Item target) {
        if(subject == null || target == null) return null;
        Item coin = subject;
        Item piggy = target;
        if(!coin.isCoin()){
            coin = target;
            piggy = subject;
        }
        if(!performer.isPlayer() || !coin.isCoin()
                || piggy.getTemplateId() != PiggyBank.getInstance().getPiggyBankTemplateId()) return null;
        return Collections.singletonList(actionEntry);
    }

    public boolean action(Action action, Creature performer, Item subject, Item target, short num, float counter) {
        if(subject == null || target == null) return true;
        Item coin = subject;
        Item piggy = target;
        if(!coin.isCoin()){
            coin = target;
            piggy = subject;
        }
        if (!performer.isPlayer() || !coin.isCoin() ||
                piggy.getTemplateId() != PiggyBank.getInstance().getPiggyBankTemplateId())
            return defaultPropagation(action);
        return PiggyBank.depositCoin(performer, coin, piggy);
    }

    @Override
    public short getActionId() {
        return this.actionId;
    }
}
