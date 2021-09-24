package xerca.xercamod.common.crafting;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.function.Supplier;

public class RecipeConditionSmoking extends SmokingRecipe {
    private final Supplier<Boolean> condition;
    private RecipeSerializer serializer;

    public RecipeConditionSmoking(ResourceLocation idIn, String groupIn, Ingredient ingredientIn, ItemStack resultIn, float experienceIn, int cookTimeIn, Supplier<Boolean> condition) {
        super(idIn, groupIn, ingredientIn, resultIn, experienceIn, cookTimeIn);
        this.condition = condition;
    }

    public RecipeConditionSmoking(SmokingRecipe SmokingRecipe, Supplier<Boolean> condition, RecipeSerializer serializer){
        super(SmokingRecipe.getId(), SmokingRecipe.getGroup(), SmokingRecipe.getIngredients().get(0), SmokingRecipe.getResultItem(), SmokingRecipe.getExperience(), SmokingRecipe.getCookingTime());
        this.condition = condition;
        this.serializer = serializer;
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    @Override
    public boolean matches(Container inv, Level worldIn) {
        if(!condition.get()){
            return false;
        }
        return super.matches(inv, worldIn);
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    @Override
    public ItemStack assemble(Container inv) {
        if(!condition.get()){
            return ItemStack.EMPTY;
        }
        return super.assemble(inv);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    public void setSerializer(RecipeSerializer<?> serializer) {
        this.serializer = serializer;
    }

    public static class Serializer extends ForgeRegistryEntry<RecipeSerializer<?>> implements RecipeSerializer<RecipeConditionSmoking> {
        private static final SimpleCookingSerializer<SmokingRecipe> furnaceSerializer = RecipeSerializer.SMOKING_RECIPE;
        private final Supplier<Boolean> condition;

        public Serializer(Supplier<Boolean> condition){
            this.condition = condition;
        }

        public RecipeConditionSmoking fromJson(ResourceLocation recipeId, JsonObject json) {
            SmokingRecipe SmokingRecipe = furnaceSerializer.fromJson(recipeId, json);
            return new RecipeConditionSmoking(SmokingRecipe, condition, this);
        }

        public RecipeConditionSmoking fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            SmokingRecipe SmokingRecipe = furnaceSerializer.fromNetwork(recipeId, buffer);
            return new RecipeConditionSmoking(SmokingRecipe, condition, this);
        }

        public void toNetwork(FriendlyByteBuf buffer, RecipeConditionSmoking recipe) {
            furnaceSerializer.toNetwork(buffer, recipe);
        }


    }
}