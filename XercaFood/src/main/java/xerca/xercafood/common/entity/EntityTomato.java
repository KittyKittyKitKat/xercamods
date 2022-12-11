package xerca.xercafood.common.entity;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import xerca.xercafood.common.SoundEvents;
import xerca.xercafood.common.item.Items;

public class EntityTomato extends ThrowableItemProjectile {

    public EntityTomato(EntityType<? extends EntityTomato> type, Level world) {
        super(type, world);
    }

    public EntityTomato(Level worldIn, LivingEntity throwerIn) {
        super(Entities.TOMATO, throwerIn, worldIn);
    }

    public EntityTomato(Level worldIn, double x, double y, double z) {
        super(Entities.TOMATO, x, y, z, worldIn);
    }

    public EntityTomato(Level worldIn) {
        super(Entities.TOMATO, worldIn);
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityRayTraceResult = (EntityHitResult) result;
            entityRayTraceResult.getEntity().hurt(DamageSource.thrown(this, this.getOwner()), 1f);
        }

        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte)3);
            level.playSound(null, result.getLocation().x, result.getLocation().y, result.getLocation().z, SoundEvents.TOMATO_SPLASH, SoundSource.PLAYERS, 1.0f, this.random.nextFloat() * 0.2F + 0.9F);
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void defineSynchedData() {

    }

    public void handleEntityEvent(byte id)
    {
        if (id == 3)
        {
            for (int j = 0; j < 8; ++j) {
                this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.ITEM_TOMATO)), this.getX(), this.getY(), this.getZ(), ((double) this.random.nextFloat() - 0.5D) * 0.28D, ((double) this.random.nextFloat() - 0.3D) * 0.28D, ((double) this.random.nextFloat() - 0.5D) * 0.28D);
            }
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ITEM_TOMATO;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.ITEM_TOMATO);
    }
}
