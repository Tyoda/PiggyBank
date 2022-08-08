package org.tyoda.wurm.PiggyBank;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.*;
import com.wurmonline.shared.constants.IconConstants;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.gotti.wurmunlimited.modsupport.items.ModItems;
import org.gotti.wurmunlimited.modsupport.items.ModelNameProvider;
import org.jetbrains.annotations.Nullable;
import org.tyoda.wurm.Iconzz.Iconzz;
import org.tyoda.wurm.PiggyBank.actions.BreakPiggyBankAction;
import org.tyoda.wurm.PiggyBank.actions.PutInPiggyBankAction;
import org.tyoda.wurmunlimited.mods.CommonLibrary.LootTable;
import org.tyoda.wurmunlimited.mods.CommonLibrary.SimpleProperties;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;

public class PiggyBank implements WurmServerMod, PreInitable, Configurable, ServerStartedListener, ItemTemplatesCreatedListener, Versioned {
    public static final String version = "1.0";

    /**
     * Template ID of the piggy bank item
     */
    private int piggyBankTemplateId = -1;

    /**
     * Template ID of the clay piggy bank item
     */
    private int piggyBankClayTemplateId = -1;

    /**
     * Model name for the clay piggy bank
     */
    private String clayModelName = "model.mod.commontweaks.piggybank.clay";

    /**
     * Model name for the pottery piggy bank
     */
    private String potteryModelName = "model.mod.commontweaks.piggybank.pottery";

    /**
     * Items the piggy banks can be broken on
     */
    private int[] breakOnItems = new int[]{180/*forge*/, 185/*largeAnvil*/, 323/*altarStone*/, 324/*altarGold*/,
            325/*altarSilver*/, 327/*altarHoly*/, 328/*altarUnholy*/, 398/*statueNymph*/, 399/*statueDemon*/,
            400/*statueDog*/, 401/*statueTroll*/, 402/*statueBoy*/, 403/*statueGirl*/, 404/*stoneBench*/,
            405/*stoneFountainDrink*/, 406/*stoneSlab*/, 407/*stoneCoffin*/, 408/*stoneFountain*/,
            430/*guardTowerHots*/, 490/*boatRowing*/, 491/*boatSailing*/, 518/*colossus*/, 528/*guardTowerMol*/,
            539/*cartLarge*/, 540/*cog*/, 541/*corbita*/, 542/*knarr*/, 543/*caravel*/, 592/*mineDoorPlanks*/,
            593/*mineDoorStone*/, 594/*mineDoorGold*/, 595/*mineDoorSilver*/, 596/*mineDoorSteel*/, 608/*stoneWell*/,
            635/*stoneFountain2*/, 638/*guardTowerFreedom*/, 732/*portalEpic*/, 733/*portalEpicHuge*/,
            821/*gravestone*/, 822/*gravestoneBuries*/, 869/*colossusOfVynora*/, 870/*colossusOfMagranon*/,
            889/*openFirePlace*/, 891/*woodenBench*/, 892/*wardrobe*/, 893/*woodenCoffer*/, 894/*royalThrone*/,
            905/*stoneKeystone*/, 906/*marbleKeystone*/, 907/*colossusOfFo*/, 911/*highBookshelf*/, 912/*lowBookshelf*/,
            916/*colossusOfLibila*/, 927/*cupBoard*/, 928/*roundMarbleTable*/, 929/*rectMarbleTable*/, 1023/*kiln*/,
            1277/*larder*/, 1323/*statueEagle*/, 1324/*statueWorg*/, 1325/*statueHellHorse*/, 1326/*statueVynora*/,
            1327/*statueMagranon*/, 1328/*statueFo*/, 1329/*statueLibila*/, 1330/*statueDrake*/,
            1400/*emptyLowBookshelf*/, 1401/*emptyHighBookshelf*/, 1415/*statueUnicorn*/, 1416/*statueGoblin*/,
            1417/*statueFiend*/
    };
    /**
     * Tiles piggy banks can be broken on
     */
    private byte[] breakOnTiles = new byte[]{4/*rock*/, 9/*cobblestone*/, 15/*planks*/, 16/*stoneSlabs*/, 17/*Gravel*/,
            21/*cliff*/, 25/*mineDoorWood*/, 26/*mineDoorStone*/, 27/*mineDoorGold*/, 28/*mineDoorSilver*/,
            29/*mineDoorSteel*/, 36/*slateBricks*/, 37/*marbleSlabs*/, 39/*planksTarred*/, 41/*cobblestoneRough*/,
            42/*cobblestoneRound*/, 44/*sandstoneBricks*/, 45/*sandstoneSlabs*/, 46/*slateSlabs*/, 47/*marbleBricks*/,
            48/*potteryBricks*/, 49/*preparedBridge*/, (byte)200/*cave*/, (byte)201/*caveExit*/,
            (byte)207/*caveReinforced*/, (byte)246/*cavePrepared*/
    };

    /**
     * Whether to ignore breakOnItems and break on all items.
     */
    private boolean breakOnAllItems = false;

    /**
     * Whether to ignore breakOnTiles and break on all tiles.
     */
    private boolean breakOnAllTiles = false;

    private static PiggyBank instance;

    public static final Logger logger = Logger.getLogger(PiggyBank.class.getName());

    public PiggyBank(){
        instance = this;
    }

    public void configure(Properties properties) {
        SimpleProperties p = new SimpleProperties(properties);

        logger.info("Configuring piggy bank");
        breakOnItems = p.getIntArray("breakOnItems", breakOnItems);
        breakOnAllItems = p.getBoolean("breakOnAllItems", breakOnAllItems);
        breakOnTiles = p.getByteArray("breakOnTiles", breakOnTiles);
        breakOnAllTiles = p.getBoolean("breakOnAllTiles", breakOnAllTiles);
        clayModelName = p.getString("clayModelName", clayModelName);
        potteryModelName = p.getString("potteryModelName", potteryModelName);
        logger.info("Done configuring piggy bank");
    }

    public void preInit(){
        // inject code for piggy bank
        // public final boolean moveToItem(Creature mover, long targetId, boolean lastMove)
        try {
            logger.info("injecting piggy bank");
            ClassPool classPool = HookManager.getInstance().getClassPool();
            CtClass ctItem = classPool.getCtClass("com.wurmonline.server.items.Item");
            CtMethod moveToItem = ctItem.getDeclaredMethod("moveToItem");
            moveToItem.insertBefore(
            "{if(this.isCoin() " +
                    "&& com.wurmonline.server.Items.getItem(targetId).getTemplateId() " +
                    "== org.tyoda.wurm.PiggyBank.PiggyBank.getInstance().getPiggyBankTemplateId()){" +
                    "return org.tyoda.wurm.PiggyBank.PiggyBank.depositCoin(mover, this, com.wurmonline.server.Items.getItem(targetId));" +
                    "}" +
                "}");
            // org.tyoda.wurm.CommonTweaks.CommonTweaks.logger.info("Checking for item moving");
            logger.info("successfully injected piggy bank");
        }catch(NotFoundException e){ logger.severe(e.toString()); }
        catch(javassist.CannotCompileException e){
            logger.severe("Could not compile bytecode injection");
            throw new RuntimeException(e);
        }
    }
    public void onItemTemplatesCreated() {
        logger.info("Starting piggy bank onItemTemplatesCreated");


        // register icons
        Iconzz iconzz = Iconzz.getInstance();
        short clayIcon = iconzz.addIcon("PiggyClay", "mods/PiggyBank/icons/PiggyClay.png");
        clayIcon = clayIcon != -1 ? clayIcon : IconConstants.ICON_CONTAINER_CHEST;
        short potteryIcon = iconzz.addIcon("PiggyPottery", "mods/PiggyBank/icons/PiggyPottery.png");
        potteryIcon = potteryIcon != -1 ? potteryIcon : IconConstants.ICON_CONTAINER_CHEST;

        try {
            ModItems.init();
            ItemTemplate piggyBankClayTemplate = new ItemTemplateBuilder("ClayPiggyBank")
                    .name("clay piggy bank", "clay piggy banks", "A piggy bank that could be fired in a kiln.")
                    .itemTypes(new short[]{108, 196, 44, 147, 194, 63, 1})
                    .imageNumber(clayIcon)
                    .decayTime(Long.MAX_VALUE)
                    .containerSize(20, 20, 20)
                    .modelName(clayModelName)
                    .difficulty(12.0f)
                    .material((byte)18)
                    .weightGrams(750)
                    .behaviourType((short)1)
                    .build();
            piggyBankClayTemplateId = piggyBankClayTemplate.getTemplateId();
            ItemTemplate piggyBankPotteryTemplate = new ItemTemplateBuilder("PotteryPiggyBank")
                    .name("piggy bank", "pottery piggy banks", "A cute piggy bank. You get the urge to throw it at something hard.")
                    .itemTypes(new short[]{108, 30, 123, 195, 194, 52, 92, 48, 1})
                    .imageNumber(potteryIcon)
                    .decayTime(Long.MAX_VALUE)
                    .containerSize(20, 20, 20)
                    .modelName(potteryModelName)
                    .difficulty(5.0f)
                    .material((byte)19)
                    .weightGrams(500)
                    .behaviourType((short)1)
                    .build();
            piggyBankTemplateId = piggyBankPotteryTemplate.getTemplateId();
            CreationEntryCreator.createSimpleEntry(1011, 14, 130, piggyBankClayTemplate.getTemplateId(), false, true, 0.0F, false, false, CreationCategories.POTTERY);
            TempStates.addState(new TempState(piggyBankClayTemplate.getTemplateId(), piggyBankPotteryTemplate.getTemplateId(), (short)10000, true, false, false));
        } catch (IOException e) { logger.warning(e.getMessage()); }

        // register model provider
        ModelNameProvider modelProvider = new PiggyModelProvider();
        ModItems.addModelNameProvider(piggyBankTemplateId, modelProvider);
        ModItems.addModelNameProvider(piggyBankClayTemplateId, modelProvider);

        logger.info("Done with piggy bank onItemTemplatesCreated");
    }
    public void onServerStarted(){
        ModActions.registerAction(new PutInPiggyBankAction());
        ModActions.registerAction(new BreakPiggyBankAction());
    }

    public static boolean depositCoin(Creature performer, Item coin, Item piggyBank){
        int coinValue = coin.getValue();
        if(piggyBank.getData1() == -1) piggyBank.setData1(0);
        piggyBank.setData1(piggyBank.getData1() + coinValue);
        piggyBank.setWeight(piggyBank.getWeightGrams(false)+coin.getWeightGrams(false), false, true);
        Items.destroyItem(coin.getWurmId(), false, true);
        performer.getCommunicator().
                sendNormalServerMessage("You deposit the "+Item.getMaterialString(coin.getMaterial())+" "+coin.getName()+" in the piggy bank.");
        return true;
    }

    public static boolean breakPiggy(Creature performer, Item piggyBank, int targetX, int targetY, @Nullable Item target){
        if(Math.abs(performer.getTileX() - targetX) > 1 || Math.abs(performer.getTileY() - targetY) > 1){
            performer.getCommunicator().sendNormalServerMessage("There's no way you can throw the piggy bank that far.");
        }else {
            try {
                ArrayList<Item> coins = LootTable.createItems(LootTable.generateCoins(piggyBank.getData1()), 0, 0, 0);
                StringBuilder message = new StringBuilder("You break the piggy bank ");
                StringBuilder serverMessage = new StringBuilder(performer.getName())
                        .append(" breaks ")
                        .append(performer.getHisHerItsString())
                        .append(" piggy bank ");
                if (target != null) {
                    message.append("on the ")
                            .append(target.getName())
                            .append(' ');
                    serverMessage.append("on the ")
                            .append(target.getName())
                            .append(' ');
                }
                if (coins.size() != 0) {
                    Item inventory = performer.getInventory();
                    for (Item coin : coins)
                        inventory.insertItem(coin, true);
                    if (coins.size() == 1) {
                        message.append("and find a single coin inside. You pick it up.");
                        serverMessage.append("and picks up the single coin ")
                                .append(performer.getHisHerItsString())
                                .append(" finds inside.");
                    } else {
                        message.append("and quickly gather the spilled coins.");
                        serverMessage.append("and quickly gathers the spilled coins.");
                    }
                } else {
                    message.append("to find nothing inside.");
                    serverMessage.append("and looks surprised as ")
                            .append(performer.getHeSheItString())
                            .append(" finds no coins inside.");
                }
                performer.getCommunicator().sendNormalServerMessage(message.toString());
                Server.getInstance().broadCastAction(serverMessage.toString(), performer, 5);
                Items.destroyItem(piggyBank.getWurmId(), false, true);
            } catch (FailedException e) {
                performer.getCommunicator().sendNormalServerMessage("You tried to break the piggy bank but miss the " + (target != null ? target.getName() : (performer.getBuildingId() == -10L ? "ground" : "floor")) + ".");
                logger.severe("Failed while creating coins: " + e.getMessage());
            }
        }
        return true;
    }

    public boolean breaksOnItem(int templateId){
        if(breakOnAllItems)
            return true;
        for(int id : breakOnItems){
            if(id == templateId)
                return true;
        }
        return false;
    }

    public boolean breaksOnTile(byte type){
        if(breakOnAllTiles)
            return true;
        for(byte t : breakOnTiles){
            if(t == type)
                return true;
        }
        return false;
    }
    public int getPiggyBankTemplateId() {
        return piggyBankTemplateId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public static PiggyBank getInstance(){
        return instance;
    }
}