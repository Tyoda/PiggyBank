package org.tyoda.wurm.PiggyBank;

import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.items.ModelNameProvider;

public class PiggyModelProvider implements ModelNameProvider {
    @Override
    public String getModelName(Item item) {
        StringBuilder sb = new StringBuilder(item.getTemplate().getModelName());

        //if (item.getDamage() >= 50f)
        //    sb.append("decayed.");

        return sb.toString();
    }
}