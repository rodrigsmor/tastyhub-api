package com.rodrigo.tastyhub.shared.infrastructure.seed;

import com.rodrigo.tastyhub.modules.recipes.domain.model.*;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.CurrencyRepository;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.IngredientRepository;
import com.rodrigo.tastyhub.modules.recipes.domain.repository.RecipeRepository;
import com.rodrigo.tastyhub.modules.settings.domain.model.UserSettings;
import com.rodrigo.tastyhub.modules.tags.domain.model.Tag;
import com.rodrigo.tastyhub.modules.tags.domain.repository.TagRepository;
import com.rodrigo.tastyhub.modules.user.domain.model.Role;
import com.rodrigo.tastyhub.modules.user.domain.model.User;
import com.rodrigo.tastyhub.modules.user.domain.model.UserRole;
import com.rodrigo.tastyhub.modules.user.domain.repository.RoleRepository;
import com.rodrigo.tastyhub.modules.user.domain.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TagRepository tagRepository;
    private final CurrencyRepository currencyRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    public DevDataSeeder(
        RoleRepository roleRepository,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        CurrencyRepository currencyRepository,
        TagRepository tagRepository,
        RecipeRepository recipeRepository,
        IngredientRepository ingredientRepository
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currencyRepository = currencyRepository;
        this.tagRepository = tagRepository;
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
    }

// TO-DO  criar relacoes de follows, criar comments nas recipes, criar salvamento das receitas, criar collections com as receitas, atualizar estatisticas das receitas

    @Transactional
    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
            seedRecipes();
        }
    }

    private void seedUsers() {
        String defaultPassword = passwordEncoder.encode("!P@ssword123");

        Role role = roleRepository.findByName(UserRole.ROLE_USER)
            .orElseThrow(RuntimeException::new);

        for (UserSeedData data : seedData) {
            String username = data.email().split("@")[0];

            User user = User.builder()
                .firstName(data.firstName())
                .lastName(data.lastName())
                .username(username)
                .email(data.email())
                .password(defaultPassword)
                .dateOfBirth(data.birthDate())
                .bio(data.bio())
                .roles(Set.of(role))
                .phone(data.phone())
                .coverPhotoUrl(data.coverUrl())
                .profilePictureUrl(data.photoUrl())
                .coverPhotoUrl(data.coverAlt())
                .profilePictureAlt(data.photoAlt())
                .build();

            UserSettings settings = UserSettings.builder()
                .user(user)
                .build();

            user.setSettings(settings);

            user.completeOnboarding();
            user.createDefaultCollections();

            userRepository.save(user);
        }
    }

    private void seedRecipes() {
        // Create Recipes
        User liam = userRepository.findByEmail("liam.smith@example.com").orElseThrow();
        User olivia = userRepository.findByEmail("oliviajohn@example.com").orElseThrow();
        User tanaka = userRepository.findByEmail("tanakayuki@example.com").orElseThrow();
        User thiago = userRepository.findByEmail("sofia.barbose@example.com").orElseThrow();
        User moretti = userRepository.findByEmail("moretti.gio@example.com").orElseThrow();
        User amarantos = userRepository.findByEmail("amarantos@example.com").orElseThrow();

        // Comments & favorite
        User valentina = userRepository.findByEmail("valentina-dasilva.rs@example.com").orElseThrow();
        User mateo = userRepository.findByEmail("mateo.rodriguez@example.com").orElseThrow();
        User jean = userRepository.findByEmail("jean-pierre@example.com").orElseThrow();
        User lari = userRepository.findByEmail("lari.ferreirasantos@example.com").orElseThrow();
        User ahmed = userRepository.findByEmail("ahmedalfarsi@example.com").orElseThrow();
        User sofia = userRepository.findByEmail("sofia.barbose@example.com").orElseThrow();
        User klaus = userRepository.findByEmail("klaus@example.com").orElseThrow();

        Currency usd = currencyRepository.save(new Currency(null, "USD", "US Dollar", "$"));
        Currency brl = currencyRepository.save(new Currency(null, "BRL", "Real Brasileiro", "R$"));

        Tag tagPasta = tagRepository.save(new Tag(null, "Pasta"));
        Tag tagBakery = tagRepository.save(new Tag(null, "Bakery"));
        Tag tagBrazillian = tagRepository.save(new Tag(null, "Brazillian"));
        Tag tagJapanese = tagRepository.save(new Tag(null, "Japanese"));
        Tag tagFishFood = tagRepository.save(new Tag(null, "Fish Food"));
        Tag tagAsian = tagRepository.save(new Tag(null, "Asian Food"));
        Tag tagHealthy = tagRepository.save(new Tag(null, "Healthy"));

        Ingredient pastaIng = ingredientRepository.save(new Ingredient(null, "Spaghetti"));
        Ingredient eggIng = ingredientRepository.save(new Ingredient(null, "Eggs"));
        Ingredient cheeseIng = ingredientRepository.save(new Ingredient(null, "Pecorino Romano"));
        Ingredient porkIng = ingredientRepository.save(new Ingredient(null, "Guanciale"));
        Ingredient tortillas = ingredientRepository.save(new Ingredient(null, "Corn Tortillas"));
        Ingredient pork = ingredientRepository.save(new Ingredient(null, "Pork Shoulder"));

        createRecipeOne(liam, usd, tagPasta, pastaIng, eggIng, cheeseIng, porkIng, valentina, ahmed, sofia);
        createRecipeTwo(olivia, usd, tagBakery);
        createRecipeThree(thiago, brl, tagBrazillian, pork, tortillas);
        createRecipeFour(moretti, usd, pork, tortillas);
        createRecipeFive(tanaka, usd, Set.of(tagJapanese, tagHealthy, tagAsian, tagFishFood));
        createRecipeSix(amarantos, brl, tagBrazillian, tagHealthy);
    }

    private void createRecipeOne(
        User author,
        Currency currency,
        Tag tag,
        Ingredient pasta,
        Ingredient egg,
        Ingredient cheese,
        Ingredient pork,
        User valentina,
        User ahmed,
        User sofia
    ) {
        RecipeStatistics statistics = new RecipeStatistics();

        Recipe recipe = Recipe.builder()
            .title("Authentic Spaghetti Carbonara")
            .description("A classic Roman pasta dish made with eggs, hard cheese, cured pork, and black pepper. No cream involved!")
            .cookTimeMin(15)
            .cookTimeMax(20)
            .estimatedCost(new BigDecimal("12.50"))
            .category(RecipeCategory.PASTA)
            .currency(currency)
            .author(author)
            .coverUrl("https://images.unsplash.com/photo-1612874742237-6526221588e3?q=80&w=800")
            .coverAlt("Close up of spaghetti carbonara with pecorino cheese")
            .tags(Set.of(tag))
            .statistics(statistics)
            .build();

        statistics.setRecipe(recipe);

        recipe.addIngredient(pasta, new BigDecimal("500"), IngredientUnitEnum.GRAM);
        recipe.addIngredient(egg, new BigDecimal("4"), IngredientUnitEnum.UNIT);
        recipe.addIngredient(cheese, new BigDecimal("100"), IngredientUnitEnum.GRAM);
        recipe.addIngredient(pork, new BigDecimal("150"), IngredientUnitEnum.GRAM);

        recipe.addStep(new PreparationStep(null, 1, "Boil a large pot of salted water and cook spaghetti until al dente.", recipe));
        recipe.addStep(new PreparationStep(null, 2, "Whisk eggs and grated Pecorino Romano in a small bowl with plenty of black pepper.", recipe));

        recipe.addComment(valentina, BigDecimal.valueOf(4.5), "Senectus netus suscipit auctor curabitur facilisi cubilia curae. 😋");
        recipe.addComment(ahmed, BigDecimal.valueOf(5), "Cursus mi pretium tellus duis convallis tempus leo. Arcu dignissim velit aliquam imperdiet mollis nullam volutpat. Montes nascetur ridiculus mus donec rhoncus eros lobortis. Adipiscing elit quisque faucibus ex sapien vitae pellentesque. 👏🏾🥰");
        recipe.addComment(sofia, BigDecimal.valueOf(4), "itae pellentesque sem placerat in id cursus mi. Euismod quam justo lectus commodo augue arcu dignissim.");

        recipeRepository.save(recipe);
    }

    private void createRecipeTwo(User author, Currency currency, Tag tag) {
        Ingredient water = ingredientRepository.save(new Ingredient(null, "Water"));
        Ingredient flour = ingredientRepository.save(new Ingredient(null, "Flour"));
        Ingredient salt = ingredientRepository.save(new Ingredient(null, "Salt"));
        Ingredient yeast = ingredientRepository.save(new Ingredient(null, "Yeast"));

        RecipeStatistics statistics = new RecipeStatistics();

        Recipe recipe = Recipe.builder()
            .title("Artisan Sourdough Bread")
            .description("Crispy golden crust with a soft, airy interior. This recipe uses a 75% hydration dough for the perfect crumb.")
            .cookTimeMin(60)
            .cookTimeMax(120)
            .estimatedCost(new BigDecimal("5.00"))
            .category(RecipeCategory.SNACK)
            .currency(currency)
            .author(author)
            .coverUrl("https://images.unsplash.com/photo-1585478259715-876a6a81b294?q=80&w=800")
            .coverAlt("A loaf of sourdough bread on a wooden board")
            .tags(Set.of(tag))
            .statistics(statistics)
            .build();

        statistics.setRecipe(recipe);

        recipe.addIngredient(flour, new BigDecimal("500"), IngredientUnitEnum.GRAM);
        recipe.addIngredient(water, new BigDecimal("375"), IngredientUnitEnum.MILLILITER);
        recipe.addIngredient(salt, new BigDecimal("10"), IngredientUnitEnum.GRAM);
        recipe.addIngredient(yeast, new BigDecimal("100"), IngredientUnitEnum.GRAM);

        recipe.addStep(new PreparationStep(null, 1, "Mix flour and water, let it rest for 1 hour (autolyse).", recipe));
        recipe.addStep(new PreparationStep(null, 2, "Add the sourdough starter and salt, then perform stretch and folds every 30 minutes.", recipe));

        recipeRepository.save(recipe);
    }

    private void createRecipeThree(User author, Currency currency, Tag tag, Ingredient pork, Ingredient tortillas) {
        // Moqueca
        Ingredient fish = ingredientRepository.save(new Ingredient(null, "White Fish Fillet"));
        Ingredient coconutMilk = ingredientRepository.save(new Ingredient(null, "Coconut Milk"));
        Ingredient dendeOil = ingredientRepository.save(new Ingredient(null, "Dendê Oil"));
        Ingredient bellPeppers = ingredientRepository.save(new Ingredient(null, "Bell Peppers"));

        // Tacos
        Ingredient pineapple = ingredientRepository.save(new Ingredient(null, "Fresh Pineapple"));
        Ingredient achiote = ingredientRepository.save(new Ingredient(null, "Achiote Paste"));

        // Tacacá
        Ingredient tucupi = ingredientRepository.save(new Ingredient(null, "Tucupi Liquid"));
        Ingredient jambu = ingredientRepository.save(new Ingredient(null, "Jambu Leaves"));
        Ingredient driedShrimp = ingredientRepository.save(new Ingredient(null, "Dried Shrimp"));
        Ingredient maniocStarch = ingredientRepository.save(new Ingredient(null, "Manioc Starch (Goma)"));

        RecipeStatistics statistics1 = new RecipeStatistics();

        Recipe recipe = Recipe.builder()
            .title("Moqueca Baiana de Peixe")
            .description("Um cozido de peixe brasileiro clássico com leite de coco, azeite de dendê e pimentões coloridos.")
            .cookTimeMin(40)
            .cookTimeMax(50)
            .estimatedCost(new BigDecimal("85.00"))
            .category(RecipeCategory.FISH)
            .currency(currency)
            .author(author)
            .coverUrl("https://images.unsplash.com/photo-1534790545184-d62f0980164c?q=80&w=800")
            .coverAlt("Moqueca fumegante em uma panela de barro preta")
            .tags(Set.of(tag))
            .statistics(statistics1)
            .build();

        statistics1.setRecipe(recipe);

        recipe.addIngredient(fish, new BigDecimal("800"), IngredientUnitEnum.GRAM);
        recipe.addIngredient(coconutMilk, new BigDecimal("200"), IngredientUnitEnum.MILLILITER);
        recipe.addIngredient(dendeOil, new BigDecimal("2"), IngredientUnitEnum.TABLESPOON);
        recipe.addIngredient(bellPeppers, new BigDecimal("3"), IngredientUnitEnum.UNIT);

        recipe.addStep(new PreparationStep(null, 1, "Marinate the fish with lime juice, garlic, and salt for 30 minutes.", recipe));
        recipe.addStep(new PreparationStep(null, 2, "Layer onions and peppers in a clay pot, add the fish, coconut milk, and dendê oil.", recipe));

        RecipeStatistics statistics2 = new RecipeStatistics();

        Recipe recipe2 = Recipe.builder()
            .title("Tacos al Pastor")
            .description("Authentic Mexican street tacos featuring marinated pork, pineapple, and fresh corn tortillas. A blast of flavor in every bite!")
            .cookTimeMin(30)
            .cookTimeMax(45)
            .estimatedCost(new BigDecimal("128.50"))
            .category(RecipeCategory.MEAL)
            .currency(currency)
            .author(author)
            .coverUrl("https://images.unsplash.com/photo-1552332386-f8dd00dc2f85?q=80&w=800")
            .coverAlt("Three corn tacos with pork and cilantro")
            .tags(Set.of(tag))
            .statistics(statistics2)
            .build();

        statistics2.setRecipe(recipe2);

        recipe2.addIngredient(pork, new BigDecimal("1"), IngredientUnitEnum.KILOGRAM);
        recipe2.addIngredient(pineapple, new BigDecimal("0.5"), IngredientUnitEnum.UNIT);
        recipe2.addIngredient(tortillas, new BigDecimal("12"), IngredientUnitEnum.UNIT);
        recipe2.addIngredient(achiote, new BigDecimal("50"), IngredientUnitEnum.GRAM);

        recipe2.addStep(new PreparationStep(null, 1, "Marinate the pork in achiote paste, chiles, and pineapple juice for 4 hours.", recipe2));
        recipe2.addStep(new PreparationStep(null, 2, "Grill the meat and thinly slice it. Serve on warm tortillas with raw onions and pineapple.", recipe2));
        recipe2.addStep(new PreparationStep(null, 3, "Tempus leo eu aenean sed diam urna tempor. Pulvinar vivamus fringilla lacus nec metus bibendum egestas. Iaculis massa nisl malesuada lacinia integer nunc posuere.", recipe2));
        recipe2.addStep(new PreparationStep(null, 4, "Ut hendrerit semper vel class aptent taciti sociosqu. Ad litora torquent per conubia.", recipe2));

        RecipeStatistics statistics3 = new RecipeStatistics();

        Recipe recipe3 = Recipe.builder()
            .title("Tacacá Paraense - O Sabor do Norte!")
            .description(
                "O tacacá é preparado com um caldo fino de cor amarelada, chamado de tucupi (originado da mandioca brava), sobre o qual se coloca goma, camarão e jambu.\n" +
                "\n" +
                "Esse é um prato que deve ser servido muito quente e deve ser temperado com sal e pimenta para realçar os sabores presentes no prato.\n" +
                "\n"
            )
            .cookTimeMin(30)
            .cookTimeMax(45)
            .estimatedCost(new BigDecimal("258.35"))
            .category(RecipeCategory.SOUP)
            .currency(currency)
            .author(author)
            .coverUrl("https://tudodelicious.com/wp-content/uploads/2025/03/Tacaca-Paraense.jpeg")
            .coverAlt("")
            .statistics(statistics3)
            .tags(Set.of(tag))
            .build();

        statistics3.setRecipe(recipe3);

        recipe3.addIngredient(tucupi, new BigDecimal("2"), IngredientUnitEnum.LITER);
        recipe3.addIngredient(jambu, new BigDecimal("2"), IngredientUnitEnum.SLICE);
        recipe3.addIngredient(driedShrimp, new BigDecimal("200"), IngredientUnitEnum.GRAM);
        recipe3.addIngredient(maniocStarch, new BigDecimal("100"), IngredientUnitEnum.GRAM);

        recipe3.addStep(new PreparationStep(null, 1, "Marinate the pork in achiote paste, chiles, and pineapple juice for 4 hours.", recipe3));
        recipe3.addStep(new PreparationStep(null, 2, "Grill the meat and thinly slice it. Serve on warm tortillas with raw onions and pineapple.", recipe3));
        recipe3.addStep(new PreparationStep(null, 3, "Tempus leo eu aenean sed diam urna tempor. Pulvinar vivamus fringilla lacus nec metus bibendum egestas. Iaculis massa nisl malesuada lacinia integer nunc posuere.", recipe3));
        recipe3.addStep(new PreparationStep(null, 4, "Ut hendrerit semper vel class aptent taciti sociosqu. Ad litora torquent per conubia.", recipe3));

        List<Recipe> recipes = new ArrayList<>(List.of(recipe, recipe2, recipe3));

        recipeRepository.saveAll(recipes);
    }

    private void createRecipeFour(User author, Currency currency, Ingredient pork, Ingredient tortillas) {
        Tag tagMexican = tagRepository.save(new Tag(null, "Mexican"));
        Tag tagDessert = tagRepository.save(new Tag(null, "Dessert"));
        Tag tagMeat = tagRepository.save(new Tag(null, "Meat"));

        Ingredient pineapple = ingredientRepository.save(new Ingredient(null, "Pineapple"));
        Ingredient creamCheese = ingredientRepository.save(new Ingredient(null, "Cream Cheese"));
        Ingredient berries = ingredientRepository.save(new Ingredient(null, "Mixed Berries"));
        Ingredient jackfruit = ingredientRepository.save(new Ingredient(null, "Green Jackfruit"));
        Ingredient bbqSauce = ingredientRepository.save(new Ingredient(null, "BBQ Sauce"));
        Ingredient spaghetti = ingredientRepository.save(new Ingredient(null, "Spaghetti Pasta"));
        Ingredient groundBeef = ingredientRepository.save(new Ingredient(null, "Ground Beef"));

        RecipeStatistics statistics1 = new RecipeStatistics();

        Recipe recipe = Recipe.builder()
            .title("Tacos al Pastor")
            .description("Authentic Mexican street tacos featuring marinated pork, pineapple, and fresh corn tortillas. A blast of flavor in every bite!")
            .cookTimeMin(30)
            .cookTimeMax(45)
            .estimatedCost(new BigDecimal("15.00"))
            .category(RecipeCategory.MEAL)
            .currency(currency)
            .author(author)
            .coverUrl("https://www.giallozafferano.com.br/images/86-8639/tacos-rapidos_1200x800.jpg")
            .coverAlt("Three corn tacos with pork and cilantro")
            .tags(Set.of(tagMexican, tagMeat))
            .statistics(statistics1)
            .build();

        statistics1.setRecipe(recipe);

        recipe.addIngredient(pork, new BigDecimal("500"), IngredientUnitEnum.GRAM);
        recipe.addIngredient(pineapple, new BigDecimal("200"), IngredientUnitEnum.GRAM);
        recipe.addIngredient(tortillas, new BigDecimal("8"), IngredientUnitEnum.UNIT);

        recipe.addStep(new PreparationStep(null, 1, "Marinate the pork in achiote paste, chiles, and pineapple juice for 4 hours.", recipe));
        recipe.addStep(new PreparationStep(null, 2, "Grill the meat and thinly slice it. Serve on warm tortillas with raw onions and pineapple.", recipe));

        RecipeStatistics statistics2 = new RecipeStatistics();

        Recipe recipe2 = Recipe.builder()
            .title("Sugar-Free Berry Cheesecake")
            .description("A guilt-free dessert using almond flour for the crust and a creamy xylitol-sweetened filling with fresh forest berries.")
            .cookTimeMin(20)
            .cookTimeMax(180)
            .estimatedCost(new BigDecimal("45.00"))
            .category(RecipeCategory.DESSERT)
            .currency(currency)
            .author(author)
            .coverUrl("https://images.unsplash.com/photo-1533134242443-d4fd215305ad?q=80&w=800")
            .coverAlt("Cheesecake slice topped with raspberries and blueberries")
            .statistics(statistics2)
            .build();

        statistics2.setRecipe(recipe2);

        recipe2.addIngredient(creamCheese, new BigDecimal("450"), IngredientUnitEnum.GRAM);
        recipe2.addIngredient(berries, new BigDecimal("1"), IngredientUnitEnum.UNIT);
        recipe2.addStep(new PreparationStep(null, 1, "Shred the jackfruit and sauté with onions, garlic, and smoked paprika until golden.", recipe2));
        recipe2.addStep(new PreparationStep(null, 2, "Simmer with your favorite BBQ sauce until tender. Serve on toasted buns with slaw.", recipe2));

        RecipeStatistics statistics3 = new RecipeStatistics();

        Recipe recipe3 = Recipe.builder()
            .title("Vegan Jackfruit 'Pulled Pork' Sandwich")
            .description("Young jackfruit sautéed in smoky BBQ sauce, served in a crusty bun with coleslaw. You won't believe it's not meat!")
            .cookTimeMin(25)
            .cookTimeMax(35)
            .estimatedCost(new BigDecimal("30.00"))
            .category(RecipeCategory.SNACK)
            .currency(currency)
            .author(author)
            .statistics(statistics3)
            .coverUrl("https://images.unsplash.com/photo-1525059696034-476775b8fca8?q=80&w=800")
            .coverAlt("Vegan burger with pulled jackfruit and purple cabbage")
            .build();

        statistics3.setRecipe(recipe3);

        recipe3.addIngredient(jackfruit, new BigDecimal("400"), IngredientUnitEnum.GRAM);
        recipe3.addIngredient(bbqSauce, new BigDecimal("150"), IngredientUnitEnum.MILLILITER);
        recipe3.addStep(new PreparationStep(null, 1, "Mix almond flour with butter to form the crust and bake for 10 minutes at 180°C.", recipe3));
        recipe3.addStep(new PreparationStep(null, 2, "Beat cream cheese with sweetener and vanilla, pour over crust and refrigerate until firm.", recipe3));

        RecipeStatistics statistics4 = new RecipeStatistics();

        Recipe recipe4 = Recipe.builder()
            .title("Spaghetti Bolognese")
            .description("Young jackfruit sautéed in smoky BBQ sauce, served in a crusty bun with coleslaw. You won't believe it's not meat!")
            .cookTimeMin(25)
            .cookTimeMax(35)
            .estimatedCost(new BigDecimal("30.00"))
            .category(RecipeCategory.SNACK)
            .currency(currency)
            .author(author)
            .tags(Set.of(tagDessert))
            .statistics(statistics4)
            .coverUrl("https://www.cookingclassy.com/wp-content/uploads/2022/05/bolognese-2.jpg")
            .coverAlt("Vegan burger with pulled jackfruit and purple cabbage")
            .build();

        statistics4.setRecipe(recipe4);

        recipe4.addIngredient(spaghetti, new BigDecimal("500"), IngredientUnitEnum.GRAM);
        recipe4.addIngredient(groundBeef, new BigDecimal("300"), IngredientUnitEnum.GRAM);

        recipe4.addStep(new PreparationStep(null, 1, "Shred the jackfruit and sauté with onions, garlic, and smoked paprika until golden.", recipe4));
        recipe4.addStep(new PreparationStep(null, 2, "Simmer with your favorite BBQ sauce until tender. Serve on toasted buns with slaw.", recipe4));

        List<Recipe> recipes = new ArrayList<>(List.of(recipe, recipe2, recipe3, recipe4));

        recipeRepository.saveAll(recipes);
    }

    private void createRecipeFive(User author, Currency currency, Set<Tag> tags) {
        Ingredient porkBones = ingredientRepository.save(new Ingredient(null, "Pork Marrow Bones"));
        Ingredient ramenNoodles = ingredientRepository.save(new Ingredient(null, "Fresh Ramen Noodles"));
        Ingredient porkBelly = ingredientRepository.save(new Ingredient(null, "Pork Belly (Chashu)"));
        Ingredient softBoiledEgg = ingredientRepository.save(new Ingredient(null, "Marinated Ajitama Egg"));
        Ingredient kombu = ingredientRepository.save(new Ingredient(null, "Dried Kombu (Seaweed)"));
        Ingredient greenOnions = ingredientRepository.save(new Ingredient(null, "Green Onions"));

        RecipeStatistics statistics = new RecipeStatistics();

        Recipe recipe = Recipe.builder()
            .title("Homemade Tonkotsu Ramen")
            .description("Rich, creamy pork bone broth simmered for 12 hours, served with handmade noodles and marinated soft-boiled egg.")
            .cookTimeMin(60)
            .cookTimeMax(720)
            .estimatedCost(new BigDecimal("22.00"))
            .category(RecipeCategory.SOUP)
            .currency(currency)
            .author(author)
            .coverUrl("https://images.unsplash.com/photo-1569718212165-3a8278d5f624?q=80&w=800")
            .coverAlt("A steaming bowl of ramen with pork belly slices")
            .tags(tags)
            .statistics(statistics)
            .build();

        statistics.setRecipe(recipe);

        recipe.addIngredient(porkBones, new BigDecimal("2"), IngredientUnitEnum.KILOGRAM);
        recipe.addIngredient(ramenNoodles, new BigDecimal("400"), IngredientUnitEnum.GRAM);
        recipe.addIngredient(porkBelly, new BigDecimal("4"), IngredientUnitEnum.SLICE);
        recipe.addIngredient(softBoiledEgg, new BigDecimal("2"), IngredientUnitEnum.UNIT);
        recipe.addIngredient(kombu, new BigDecimal("1"), IngredientUnitEnum.HALF);
        recipe.addIngredient(greenOnions, new BigDecimal("1"), IngredientUnitEnum.BUNCH);

        recipe.addStep(new PreparationStep(null, 1, "Clean and boil pork bones to remove impurities, then simmer with aromatics for 12 hours.", recipe));
        recipe.addStep(new PreparationStep(null, 2, "Assemble the bowl with noodles, tare (seasoning), the hot broth, and toppings like chashu.", recipe));

        recipeRepository.save(recipe);
    }

    private void createRecipeSix(User user, Currency brl, Tag tagHealthy, Tag tagMeal) {
        Ingredient abobora = ingredientRepository.save(new Ingredient(null, "Abóbora Cabotiá"));
        Ingredient leiteCoco = ingredientRepository.save(new Ingredient(null, "Leite de Coco"));
        Ingredient gengibre = ingredientRepository.save(new Ingredient(null, "Gengibre Fresco"));
        Ingredient sementeAbobora = ingredientRepository.save(new Ingredient(null, "Sementes de Abóbora"));
        Ingredient caldoLegumes = ingredientRepository.save(new Ingredient(null, "Caldo de Legumes"));

        RecipeStatistics statistics9 = new RecipeStatistics();

        Recipe recipe9 = Recipe.builder()
            .title("Creme de Abóbora com Gengibre")
            .description("Um creme aveludado, 100% vegano e reconfortante. O gengibre traz um toque picante que acelera o metabolismo e aquece o corpo em dias frios.")
            .cookTimeMin(30).cookTimeMax(40).estimatedCost(new BigDecimal("25.00"))
            .category(RecipeCategory.SOUP).currency(brl).author(user)
            .coverUrl("https://images.unsplash.com/photo-1476718406336-bb5a9690ee2a?q=80&w=800")
            .coverAlt("Tigela de sopa de abóbora laranja vibrante com sementes por cima")
            .tags(Set.of(tagHealthy))
            .statistics(statistics9)
            .build();

        statistics9.setRecipe(recipe9);

        recipe9.addIngredient(abobora, new BigDecimal("1"), IngredientUnitEnum.UNIT);
        recipe9.addIngredient(leiteCoco, new BigDecimal("200"), IngredientUnitEnum.MILLILITER);
        recipe9.addIngredient(gengibre, new BigDecimal("20"), IngredientUnitEnum.GRAM);
        recipe9.addIngredient(sementeAbobora, new BigDecimal("1"), IngredientUnitEnum.BUNCH);
        recipe9.addIngredient(caldoLegumes, new BigDecimal("500"), IngredientUnitEnum.MILLILITER);

        recipe9.addStep(new PreparationStep(null, 1, "Corte a abóbora em cubos e refogue com cebola e o gengibre ralado.", recipe9));
        recipe9.addStep(new PreparationStep(null, 2, "Adicione o caldo de legumes e cozinhe até que a abóbora esteja bem macia.", recipe9));
        recipe9.addStep(new PreparationStep(null, 3, "Bata tudo no liquidificador até obter uma textura lisa e retorne para a panela.", recipe9));
        recipe9.addStep(new PreparationStep(null, 4, "Adicione o leite de coco, acerte o sal e sirva com as sementes tostadas por cima.", recipe9));

        Ingredient fileMignon = ingredientRepository.save(new Ingredient(null, "Filé Mignon"));
        Ingredient vinhoTinto = ingredientRepository.save(new Ingredient(null, "Vinho Tinto Seco"));
        Ingredient manteiga = ingredientRepository.save(new Ingredient(null, "Manteiga Sem Sal"));
        Ingredient alecrim = ingredientRepository.save(new Ingredient(null, "Alecrim Fresco"));
        Ingredient batataAsterix = ingredientRepository.save(new Ingredient(null, "Batata Asterix"));

        RecipeStatistics statistics10 = new RecipeStatistics();

        Recipe recipe10 = Recipe.builder()
            .title("Medalhão ao Molho de Vinho")
            .description("Medalhões de filé mignon grelhados no ponto perfeito, acompanhados de um redução de vinho tinto artesanal e batatas rústicas ao alecrim.")
            .cookTimeMin(45).cookTimeMax(60).estimatedCost(new BigDecimal("120.00"))
            .category(RecipeCategory.MEAL)
            .currency(brl)
            .author(user)
            .coverUrl("https://images.unsplash.com/photo-1546241072-48010ad28abb?q=80&w=800")
            .coverAlt("Bife suculento com molho escuro e batatas ao lado")
            .tags(Set.of(tagMeal))
            .statistics(statistics10)
            .build();

        statistics10.setRecipe(recipe10);

        recipe10.addIngredient(fileMignon, new BigDecimal("500"), IngredientUnitEnum.GRAM);
        recipe10.addIngredient(vinhoTinto, new BigDecimal("250"), IngredientUnitEnum.MILLILITER);
        recipe10.addIngredient(manteiga, new BigDecimal("50"), IngredientUnitEnum.GRAM);
        recipe10.addIngredient(alecrim, new BigDecimal("2"), IngredientUnitEnum.BUNCH);
        recipe10.addIngredient(batataAsterix, new BigDecimal("3"), IngredientUnitEnum.UNIT);

        recipe10.addStep(new PreparationStep(null, 1, "Tempere a carne e sele os medalhões em uma frigideira bem quente com manteiga.", recipe10));
        recipe10.addStep(new PreparationStep(null, 2, "Na mesma frigideira, adicione o vinho tinto e deixe reduzir até metade do volume.", recipe10));
        recipe10.addStep(new PreparationStep(null, 3, "Corte as batatas em gomos e asse com azeite, sal e alecrim até dourarem.", recipe10));
        recipe10.addStep(new PreparationStep(null, 4, "Finalize o molho com uma colher de manteiga gelada e sirva sobre a carne.", recipe10));

        recipeRepository.saveAll(List.of(recipe9, recipe10));
    }

    private final List<UserSeedData> seedData = List.of(
        new UserSeedData(
            "Liam",
            "Smith",
            "liam.smith@example.com",
            LocalDate.of(1990, 5, 15),
            "Passionate home cook and pasta lover.",
            "+1 555-0101",
            "https://mockmind-api.uifaces.co/content/human/222.jpg",
            "https://cdn.magicdecor.in/com/2023/11/15111433/Indian-Food-Platter-Wallpaper-for-Wall.jpg",
            "A rustic kitchen background",
            "Portrait of Liam smiling"
        ),
        new UserSeedData(
            "Olivia",
            "Johson",
            "oliviajohn@example.com",
            LocalDate.of(1995, 8, 22),
            "Bakery enthusiast and pastry chef.",
            "+1 555-0202",
            "https://img.freepik.com/free-photo/top-view-recipe-book-still-life-concept_23-2149055992.jpg",
            "https://img.freepik.com/free-photo/portrait-beautiful-woman-smiling-shot_329181-17464.jpg?semt=ais_rp_progressive&w=740&q=80",
            "A tray of freshly baked croissants",
            "Olivia wearing a chef hat"
        ),
        new UserSeedData(
            "Thiago",
            "Alcântara de Oliveira",
            "thiago-alcantara@example.com",
            LocalDate.of(1988, 3, 12),
            "Cozinheiro amador focado em gastronomia brasileira regional. Amo explorar os sabores do cerrado e da Amazônia.",
            "+55 11 98888-0001",
            "https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=800",
            "https://randomuser.me/api/portraits/men/1.jpg",
            "A colorful table with feijoada and farofa",
            "Thiago at a local farmer's market"
        ),
        new UserSeedData(
            "Yuki",
            "Tanaka Matsumoto",
            "tanakayuki@example.com",
            LocalDate.of(1992, 11, 5),
            "Tokyo-born food explorer. My mission is to simplify traditional Japanese recipes for busy modern families.",
            "+81 90-1234-5678",
            "https://images.unsplash.com/photo-1580822184713-fc5400e7fe10?q=80&w=800",
            "https://randomuser.me/api/portraits/women/2.jpg",
            "Zen garden aesthetic kitchen",
            "Yuki holding a set of handcrafted chopsticks"
        ),
        new UserSeedData(
            "Valentina",
            "da Silva Ferraz",
            "valentina-dasilva.rs@example.com",
            LocalDate.of(1994, 7, 18),
            "Sommelier e criadora de conteúdo gastronômico. Acredito que o vinho certo transforma qualquer refeição simples.",
            "+55 21 97777-1234",
            "https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?q=80&w=800",
            "https://randomuser.me/api/portraits/women/3.jpg",
            "Sunlit vineyard during harvest",
            "Valentina swirling a glass of red wine"
        ),
        new UserSeedData(
            "Mateo",
            "Rodriguez Hernandez",
            "mateo.rodriguez@example.com",
            LocalDate.of(1985, 1, 30),
            "Master of the grill and taco fanatic. Bringing the authentic smoky flavors of Mexico to your home kitchen.",
            "+52 55 1234 5678",
            "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?q=80&w=800",
            "https://randomuser.me/api/portraits/men/4.jpg",
            "Traditional Mexican street food stall",
            "Mateo preparing fresh salsa verde"
        ),
        new UserSeedData(
            "Aline",
            "Cavalcanti de Albuquerque",
            "aline.cavalcanti@example.com",
            LocalDate.of(1991, 4, 25),
            "Nutricionista apaixonada por confeitaria funcional. Receitas doces, porém saudáveis, para comer sem culpa.",
            "+55 81 99988-7766",
            "https://images.unsplash.com/photo-1490474418585-ba9bad8fd0ea?q=80&w=800",
            "https://randomuser.me/api/portraits/women/5.jpg",
            "Bowl of fresh seasonal fruits",
            "Aline in her brightly lit nutrition office"
        ),
        new UserSeedData(
            "Jean-Pierre",
            "Dubois Lambert",
            "jean-pierre@example.com",
            LocalDate.of(1979, 12, 14),
            "Classic French techniques meet modern fusion. Exploring the chemistry behind every sauce and emulsion.",
            "+33 6 12 34 56 78",
            "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?q=80&w=800",
            "https://randomuser.me/api/portraits/men/6.jpg",
            "Elegant Michelin-star restaurant kitchen",
            "Jean-Pierre plating a gourmet dish"
        ),
        new UserSeedData(
            "Beatriz",
            "Mendonça de Souza",
            "beatriz_medonca@example.com",
            LocalDate.of(1997, 10, 2),
            "Vegana e ativista. Criando receitas plant-based que surpreendem até os maiores amantes de carne.",
            "+55 31 98765-4321",
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?q=80&w=800",
            "https://randomuser.me/api/portraits/women/7.jpg",
            "Lush green vegetable garden",
            "Beatriz picking fresh basil leaves"
        ),
        new UserSeedData(
            "Giovanni",
            "Moretti Rossi",
            "moretti.gio@example.com",
            LocalDate.of(1982, 6, 7),
            "Third-generation pizzaiolo. Dedicating my life to the art of Neapolitan pizza and slow-fermented dough.",
            "+39 02 1234567",
            "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=800",
            "https://randomuser.me/api/portraits/men/8.jpg",
            "Wood-fired oven with a glowing fire",
            "Giovanni tossing pizza dough in the air"
        ),
        new UserSeedData(
            "Larissa",
            "Ferreira dos Santos",
            "lari.ferreirasantos@example.com",
            LocalDate.of(1993, 2, 28),
            "Especialista em frutos do mar e culinária litorânea. O segredo está no frescor dos ingredientes.",
            "+55 71 99111-2233",
            "https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?q=80&w=800",
            "https://randomuser.me/api/portraits/women/9.jpg",
            "Blue ocean view from a beach restaurant",
            "Larissa holding a fresh lobster"
        ),
        new UserSeedData(
            "Ahmed",
            "Al-Farsi Mansour",
            "ahmedalfarsi@example.com",
            LocalDate.of(1987, 5, 19),
            "Middle Eastern flavor specialist. Spreading the love for spices, tahini, and authentic homemade hummus.",
            "+971 50 123 4567",
            "https://images.unsplash.com/photo-1541529086526-db283c563270?q=80&w=800",
            "https://randomuser.me/api/portraits/men/10.jpg",
            "Middle Eastern spice market background",
            "Ahmed serving traditional tea"
        ),
        new UserSeedData(
            "Sofia",
            "Barbosa de Magalhães",
            "sofia.barbose@example.com",
            LocalDate.of(1996, 9, 9),
            "Entusiasta de café e barismo. Transformando o ritual matinal em uma experiência sensorial inesquecível.",
            "+55 19 98123-4567",
            "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?q=80&w=800",
            "https://randomuser.me/api/portraits/women/11.jpg",
            "Cozy coffee shop interior with plants",
            "Sofia performing latte art"
        ),
        new UserSeedData(
            "Klaus",
            "Schmidt Hoffmann",
            "klaus@example.com",
            LocalDate.of(1975, 8, 21),
            "Sausage maker and beer pairing expert. Keeping the German traditions alive with a modern craft twist.",
            "+49 30 123456",
            "https://images.unsplash.com/photo-1534113414509-0eec2bfb493f?q=80&w=800",
            "https://randomuser.me/api/portraits/men/12.jpg",
            "Oktoberfest style wooden table",
            "Klaus holding a large pretzel and a beer"
        ),
        new UserSeedData(
            "Roberta",
            "Amarantos Carvalho de Santana",
            "amarantos@example.com",
            LocalDate.of(1975, 8, 21),
            "Sonhadora 😴\nApaixonada 🥰\nAmante de Gastronomia 🍽♥️\n💘Sigam para receber receitinhas! 🫶🏻",
            "+55 89 18821-1921",
            "https://thumbs.dreamstime.com/b/heart-shaped-pizza-valentine-s-day-vegetables-concept-tasty-healthy-food-love-free-lay-133960748.jpg",
            "https://static.vecteezy.com/system/resources/thumbnails/054/894/399/small/romantic-couple-under-gently-falling-snowflakes-photo.jpeg",
            "",
            ""
        )
    );
}
