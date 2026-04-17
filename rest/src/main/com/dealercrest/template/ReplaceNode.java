package com.dealercrest.template;

/**
 * Replaces the element entirely with a named fragment from FragmentRegistry.
 *
 * th:replace="fragmentName"
 *
 * The original element (its tag, attributes, and children) is discarded.
 * The fragment's compiled AST is rendered in its place.
 *
 * If the named fragment is not found in the registry, nothing is rendered.
 *
 * Example:
 *   Registration:
 *     fragRegistry.register("nav", "<nav><a href='/'>Home</a></nav>");
 *
 *   Template:
 *     <div th:replace="nav"></div>
 *
 *   Output:
 *     <nav><a href='/'>Home</a></nav>
 */
public class ReplaceNode extends Node {

    private final String          fragmentName;
    private final FragmentRegistry fragmentRegistry;

    // Set during compile() when the DirectiveRegistry becomes available.
    private DirectiveRegistry directiveRegistry;

    // The compiled fragment — resolved once and cached here.
    private Node resolvedFragment = null;
    private boolean resolved      = false;

    public ReplaceNode(String fragmentName, FragmentRegistry fragmentRegistry) {
        this.fragmentName     = fragmentName;
        this.fragmentRegistry = fragmentRegistry;
    }

    /**
     * compile() is where we first have access to the DirectiveRegistry.
     * We capture it for lazy fragment resolution, and attempt early resolution
     * in case the fragment is already registered.
     */
    @Override
    public Node compile(DirectiveRegistry registry) {
        this.directiveRegistry = registry;

        // Try to resolve now; fragment may not be registered yet (that is fine)
        Node frag = fragmentRegistry.get(fragmentName, registry);
        if (frag != null) {
            resolvedFragment = frag;
            resolved = true;
        }

        return this;
    }

    @Override
    public void render(DataModel ctx, StringBuilder out) {

        // Lazy resolution for fragments registered after compile time
        if (!resolved) {
            resolvedFragment = fragmentRegistry.get(fragmentName, directiveRegistry);
            resolved = true;
        }

        if (resolvedFragment == null) {
            return; // fragment not found — silent skip
        }

        // FragmentRegistry.get() returns the compiled <root> wrapper.
        // Render only its children to avoid emitting <root>...</root>.
        if (resolvedFragment instanceof ElementNode) {
            ElementNode root = (ElementNode) resolvedFragment;
            for (int i = 0; i < root.children.size(); i++) {
                root.children.get(i).render(ctx, out);
            }
        } else {
            resolvedFragment.render(ctx, out);
        }
    }
}
