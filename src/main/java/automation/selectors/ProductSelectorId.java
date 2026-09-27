package automation.selectors;

/** Validacion compartida para construir localizadores dinamicos de productos. */
final class ProductSelectorId {
    private ProductSelectorId() {}

    static void validate(String productId) {
        if (productId == null || !productId.matches("[a-z0-9-]+")) {
            throw new IllegalArgumentException("Identificador de producto invalido");
        }
    }
}
