package firok.irisia.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import firok.irisia.DamageSources;
import firok.irisia.Irisia;
import firok.irisia.Keys;
import firok.irisia.Util;
import firok.irisia.common.EntitySelectors;
import firok.irisia.entity.Throwables;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import thaumcraft.api.IWarpingGear;
import thaumcraft.api.ThaumcraftApi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Weapons
{
	public static final FlailWeapon FlailWood;
	public static final FlailWeapon FlailIron;
	public static final FlailWeapon FlailGold;
	public static final FlailWeapon FlailDiamond;
	public static final FlailWeapon FlailVoidMetal;
	public static final FlailWeapon FlailAdamantium;
	public static final FlailWeapon FlailMithril;
	public static final FlailWeapon FlailSolita;
	public static final FlailWeapon FlailMogiga;
	public static final FlailWeapon FlailBone;

	public static RunicLongBowWeapon ThaumiumRunicLongBow;
	public static final RunicLongBowWeapon VoidRunicLongBow;
	public static RunicLongBowWeapon AdamantiumLongBow;
	public static RunicLongBowWeapon MithrilLongBow;
	public static RunicLongBowWeapon SolitaLongBow;
	public static RunicLongBowWeapon MogigaLongBow;

	public final static ItemSword MercurialBlade;
	public final static ItemSword NightBlade;
	public static ItemSword PhaseSword;
	public final static ItemSword BerserkerSword;
	public final static ItemSword KineticBlade;
	public final static ItemSword WarpingBlade;
	public final static ItemSword SoulEater;
	public final static ItemSword LunarDagger;
	static
	{
		FlailWood=new FlailWeapon(Item.ToolMaterial.WOOD);
		FlailIron=new FlailWeapon(Item.ToolMaterial.IRON);
		FlailGold=new FlailWeapon(Item.ToolMaterial.GOLD);
		FlailDiamond=new FlailWeapon(Item.ToolMaterial.EMERALD);
		FlailVoidMetal=new FlailWeapon(ThaumcraftApi.toolMatVoid);
		FlailAdamantium=new FlailWeapon(Materials.AdamantiumTool);
		FlailMithril=new FlailWeapon(Materials.MithrilTool);
		FlailMogiga=new FlailWeapon(Materials.MogigaTool);
		FlailSolita=new FlailWeapon(Materials.SolitaTool);
		FlailBone=new FlailWeapon(Materials.BoneTool);

		VoidRunicLongBow=new RunicLongBowWeapon();
		MercurialBlade=new MercurialBladeWeapon();
		NightBlade=new NightBladeWeapon();
		BerserkerSword=new BerserkerSwordWeapon();
		KineticBlade=new KineticBladeWeapon();
		WarpingBlade=new WarpingBladeWeapon();
		SoulEater=new SoulEaterWeapon();
		LunarDagger=new LunarDaggerWeapon();
	}

	public static class FlailWeapon extends ItemSword
	{
		public final float damageFactor;
		public final float flownFactor;
		public FlailWeapon(ToolMaterial material)
		{
			this(material,true);
		}
		public FlailWeapon(ToolMaterial material,boolean use)
		{
			this(material,
					use?(material.getDamageVsEntity()+1):2f,
					use?material.getEfficiencyOnProperMaterial()/8:0.8f);
		}
		public FlailWeapon(ToolMaterial material,float damage,float flown)
		{
			super(material);
			damageFactor=damage;
			flownFactor=flown;
		}

		@Override // todo 这里以后用这个替换掉 public void onPlayerStoppedUsing(ItemStack p_77615_1_, World p_77615_2_, EntityPlayer p_77615_3_, int p_77615_4_)
		public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player)
		{
			if(world.isRemote)
				return itemStack;

			List entities=world.getEntitiesWithinAABBExcludingEntity(player,
					AxisAlignedBB.getBoundingBox(
							player.posX-2,player.posY-2,player.posZ-2,
							player.posX+2,player.posY+2,player.posZ+2));
			for(Object obj : entities)
			{
				// Irisia.log(((Entity)obj).toString(),player);
				if(obj instanceof EntityLivingBase)
				{
					// Irisia.log("hit !",player);
					itemStack.damageItem(1,player);
					EntityLivingBase enlb=(EntityLivingBase)obj;
					enlb.attackEntityFrom(DamageSources.StoneDamage,damageFactor);
					enlb.motionX+=flownFactor*(enlb.posX-player.posX);
					enlb.motionY+=flownFactor*(enlb.posY-player.posY)*0.3;
					enlb.motionZ+=flownFactor*(enlb.posZ-player.posZ);
				}
			}

			return itemStack;
		}

		@Override
		public int getMaxItemUseDuration(ItemStack itemStack)
		{
			return 12;
		}
	}
	public static class RunicLongBowWeapon extends ItemBow
	{
		@Override
		public void onPlayerStoppedUsing(ItemStack itemStack, World world, EntityPlayer player, int usedDuration)
		{
			int j = this.getMaxItemUseDuration(itemStack) - usedDuration;

			ArrowLooseEvent event = new ArrowLooseEvent(player, itemStack, j);
			MinecraftForge.EVENT_BUS.post(event);
			if (event.isCanceled())
			{
				return;
			}
			j = event.charge;

			// flag判断是否创造模式/无限附魔=是否消耗弹药
			boolean flag = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, itemStack) > 0;

			if (flag)//if (flag || player.inventory.hasItem(Items.arrow))
			{
				float f = (float)j / 20.0F;
				f = (f * f + f * 2.0F) / 3.0F;

				if ((double)f < 0.1D)
				{
					return;
				}

				if (f > 1.0F)
				{
					f = 1.0F;
				}

				Throwables.EntityRunicArrow entityarrow = new Throwables.EntityRunicArrow(world, player, 3);
				// thaumcraft:craftfail 爆炸音
				// thaumcraft:jacobs 电流音
				// todo 以后换成别的声音
				world.playSoundAtEntity(player, "thaumcraft:craftfail", 1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + f * 0.5F);

				if (!world.isRemote)
				{
					world.spawnEntityInWorld(entityarrow);
				}
			}
		}
	}
	public static class MercurialBladeWeapon extends ItemSword
	{
		public MercurialBladeWeapon()
		{
			super(ToolMaterial.IRON);
		}
		@Override
		public boolean hitEntity(ItemStack itemStack, EntityLivingBase target, EntityLivingBase player)
		{
			List entities=target.worldObj.getEntitiesWithinAABBExcludingEntity(target,
					AxisAlignedBB.getBoundingBox(target.posX-3,target.posY-3,target.posZ-3,
							target.posX+3,target.posY+3,target.posZ+3),
					EntitySelectors.SelectEntityMonstersAlive);
			if(entities.size()<=0)
			{
				target.worldObj.playSoundAtEntity(target,Keys.SoundCreepy,1,1);
				target.attackEntityFrom(DamageSource.generic,24);
				if(target.isEntityAlive())
					target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id,60,0));
			}
			else
			{
				target.attackEntityFrom(DamageSource.generic,6);
				target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id,40,0));
			}
			itemStack.damageItem(1, player);
			return true;
		}
	}
	public static class NightBladeWeapon extends ItemSword
	{
		public NightBladeWeapon()
		{
			super(ToolMaterial.IRON);
		}
		@Override
		public boolean hitEntity(ItemStack itemStack, EntityLivingBase target, EntityLivingBase player)
		{
			target.worldObj.playSoundAtEntity(target,Keys.SoundCreepy,1,1);
			if(target.worldObj.isDaytime())
			{
				target.attackEntityFrom(DamageSource.generic,13);
			}
			else
			{
				target.attackEntityFrom(DamageSource.generic,24);
				if(target.isEntityAlive())
					target.addPotionEffect(new PotionEffect(Potion.blindness.id,120,0));
			}

			itemStack.damageItem(1, player);
			return true;
		}
	}
	public static class BerserkerSwordWeapon extends ItemSword
	{
		public BerserkerSwordWeapon()
		{
			super(ToolMaterial.IRON);
		}
		@Override
		public boolean hitEntity(ItemStack itemStack, EntityLivingBase target, EntityLivingBase player)
		{
			float maxP=player.getMaxHealth();
			float nowP=player.getHealth();
			float damage=31-nowP/maxP *20;
			// 没血的时候一刀30 满血只有10
			target.attackEntityFrom(DamageSource.generic,damage);
			if(nowP/maxP<0.6) // 血量小于六成 给一个抗性提升buff
				player.addPotionEffect(new PotionEffect(Potion.resistance.id,80,0));
			if(damage>=15)
				; // todo 以后播放一个音效

			itemStack.damageItem(1, player);
			return true;
		}
		@Override
		public float func_150931_i()
		{
			return 10;
		}
	}
	public static class KineticBladeWeapon extends ItemSword
	{
		public KineticBladeWeapon()
		{
			super(ToolMaterial.IRON);
		}
		@Override
		/**
		 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
		 */
		public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer p)
		{
			if(!world.isRemote)
			{
				Irisia.log(getDamage(p),p);
			}
			p.setItemInUse(stack, this.getMaxItemUseDuration(stack));
			return stack;
		}
		public static double getDamage(EntityLivingBase en)
		{
			/*
			0.0784000015258789 走路
			0.25下落 慢
			0.45 下落 快
			1 长距离下落

			0.08
			0.2
			0.3
			0.5
			*/
			double speed=Util.getMotion(en);
			double ret;
			if(speed<0.08)
				ret= 10*speed/0.0784000015258789;
			else if(speed<0.2)
				ret= 13*speed/0.2;
			else if(speed<0.3)
				ret= 17*speed/0.3;
			else if(speed<0.5)
				ret= 22*speed/0.5;
			else ret= 29*speed;

			PotionEffect pe;
			if((pe=en.getActivePotionEffect(Potion.moveSpeed))!=null)
			{
				ret*=(1+pe.getAmplifier())*0.15;
			}

			return ret;
		}
		@Override
		public boolean hitEntity(ItemStack itemStack, EntityLivingBase target, EntityLivingBase player)
		{
			double damage= getDamage(player);

			// 没血的时候一刀30 满血只有10
			target.attackEntityFrom(DamageSource.generic,(float)damage);
			if(damage>=15)
				; // todo 以后播放一个音效

			itemStack.damageItem(1, player);
			return true;
		}
		@Override
		public float func_150931_i()
		{
			return 10;
		}
	}
	public static class WarpingBladeWeapon extends ItemSword implements IWarpingGear
	{
		public WarpingBladeWeapon()
		{
			super(ToolMaterial.IRON);
		}

		@Override
		public int getWarp(ItemStack stack, EntityPlayer entityPlayer)
		{
			NBTTagCompound tag=stack.hasTagCompound()?stack.getTagCompound():new NBTTagCompound();
			int killed=tag.hasKey("killed")?tag.getInteger("killed"):0;

			return killed/5;
		}

		@Override
		/**
		 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
		 */
		public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer p)
		{
			NBTTagCompound tag=stack.hasTagCompound()?stack.getTagCompound():new NBTTagCompound();

			int killed=tag.hasKey("killed")?tag.getInteger("killed"):0;
			tag.setInteger("killed",killed+1);

			ArrayList<String> list=new ArrayList<>();
			int i=0;
			for(String str:list)
			{
				System.out.println(i+" : "+str);
				i++;
			}
			i=0;
			Iterator<String> iter=list.iterator();
			while(iter.hasNext())
			{
				String str=iter.next();

				System.out.println(i+" : "+str);
				i++;
			}

			stack.setTagCompound(tag);
			p.setItemInUse(stack, this.getMaxItemUseDuration(stack));
			return stack;
		}
		@Override
		public boolean hitEntity(ItemStack itemStack, EntityLivingBase target, EntityLivingBase player)
		{
			double damage= 15;

			// 没血的时候一刀30 满血只有10
			target.attackEntityFrom(DamageSource.generic,(float)damage);
			float hp=target.getHealth();
			if(hp<=0)
			{
				NBTTagCompound tag=itemStack.hasTagCompound()?itemStack.getTagCompound():new NBTTagCompound();
				int killed=tag.hasKey("killed")?tag.getInteger("killed"):0;
				killed++;
				tag.setInteger("killed",killed);
				itemStack.setTagCompound(tag);
			}
//			if(damage>=15)
//				; // todo 以后播放一个音效

			itemStack.damageItem(1, player);
			return true;
		}
		@Override
		public float func_150931_i()
		{
			return 10;
		}
	}
	public static class SoulEaterWeapon extends ItemSword implements IWarpingGear
	{
		public SoulEaterWeapon()
		{
			super(ToolMaterial.IRON);
		}

		@Override
		public int getWarp(ItemStack stack, EntityPlayer entityPlayer)
		{
			return 3;
		}

		@Override
		/**
		 * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
		 */
		public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer p)
		{
			float hp=p.getHealth();
			float max=p.getMaxHealth();

			if(max-hp>=4)
			{
				NBTTagCompound tag=stack.hasTagCompound()?stack.getTagCompound():new NBTTagCompound();
				int souls=tag.hasKey("souls")?tag.getInteger("souls"):0;

				if(souls>=15)
				{
					souls-=15;
					p.heal(4);

					tag.setInteger("souls",souls);
					stack.setTagCompound(tag);
				}
			}

			p.setItemInUse(stack, this.getMaxItemUseDuration(stack));
			return stack;
		}
		@Override
		public boolean hitEntity(ItemStack itemStack, EntityLivingBase target, EntityLivingBase player)
		{
			NBTTagCompound tag=itemStack.hasTagCompound()?itemStack.getTagCompound():new NBTTagCompound();
			int souls=tag.hasKey("souls")?tag.getInteger("souls"):0;
			float damage=6;

			damage+=souls/10.0; // 每10点灵魂加1伤害
			if(damage>41) damage=41; // 最大伤害限制在41
			target.attackEntityFrom(DamageSource.generic,(float)damage);

			float hp=target.getHealth();
			if(hp<=0) souls++;// 击杀一个生物增加一个灵魂


			tag.setInteger("souls",souls);
			itemStack.setTagCompound(tag);

			itemStack.damageItem(1, player);
			return true;
		}
		@Override
		public float func_150931_i()
		{
			return 6;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public void addInformation(ItemStack stack, EntityPlayer player, List info, boolean p_77624_4_)
		{
			NBTTagCompound tag=stack.hasTagCompound()?stack.getTagCompound():new NBTTagCompound();
			int souls=tag.hasKey("souls")?tag.getInteger("souls"):0;
			info.add(StatCollector.translateToLocal(Keys.InfoSoulEaterSouls)+souls);
		}
	}
	public static class LunarDaggerWeapon extends ItemSword
	{
		public LunarDaggerWeapon()
		{
			super(ToolMaterial.IRON);
		}

		@Override
		public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean onHand)
		{
			if(onHand&&!world.isRemote&&world.getTotalWorldTime()%10==0)
			{
				if(entity.isSneaking() && entity instanceof EntityLivingBase)
				{
					((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.invisibility.id,20,0));
				}
			}
		}

		@Override
		public boolean hitEntity(ItemStack itemStack, EntityLivingBase target, EntityLivingBase player)
		{
			boolean isNight=!player.worldObj.isDaytime();
			boolean isInvisible=player.getActivePotionEffect(Potion.invisibility)!=null;
			// 夜晚攻击加成5 隐形暴击倍数3
			float damage=(isNight?8:3)*(isInvisible?3.5f:1);
			target.attackEntityFrom(DamageSource.generic,damage);

			// 移除玩家的隐形
			player.removePotionEffect(Potion.invisibility.id);
			player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id,80,1));
			player.addPotionEffect(new PotionEffect(Potion.jump.id,80,1));

			if(damage>=15)
				; // todo 以后播放一个音效

			itemStack.damageItem(1, player);
			return true;
		}

		@Override
		public int getMaxItemUseDuration(ItemStack itemStack)
		{
			return 6;
		}
	}
}
