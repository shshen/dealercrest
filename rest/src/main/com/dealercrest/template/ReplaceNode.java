package com.dealercrest.template;

/**
 * Replaces the element entirely with a named fragment from the Model's
 * per-page FragmentRegistry.
 *
 * th:replace="fragmentName"
 *
 * The original element (its tag, attributes, and children) is discarded.
 * The fragment's compiled AST is rendered in its place.
 *
 * CHANGE: The FragmentRegistry is no longer baked in at compile time.
 * It is fetched from the Model at render time via model.getFragments().
 * This means each render call can supply a completely different set of
 * fragments simply by attaching a different FragmentRegistry to the Model.
 *
 * Behaviour when no registry is available:
 *   - model.getFragments() returns null  → silent skip (render nothing)
 *   - fragment name not found in registry → silent skip (render nothing)
 *
 * Fragment compilation:
 *   Fragment ASTs are compiled and cached inside FragmentRegistry on first
 *   use.  The DirectiveRegistry needed for compilation is captured once
 *   during compile() and stored for use at render time.
 *
 * Example:
 *   Page A:
 *     FragmentRegistry fr = new FragmentRegistry();
 *     fr.register("nav", "<nav><a href='/vehicles'>Vehicles</a></nav>");
 *     model.setFragments(fr);
 *
 *   Page B (same engine, different fragments):
 *     FragmentRegistry fr = new FragmentRegistry();
 *     fr.register("nav", "<nav><a href='/profile'>Profile</a></nav>");
 *     model.setFragments(fr);
 *
 *   Template (shared, cached):
 *     <div th:replace="nav"></div>
 *     → each page gets its own nav output
 */
public class ReplaceNode extends Node {

    private final String fragmentName;

    /**
     * Captured during compile() — needed to compile fragment HTML into an AST
     * the first time a given FragmentRegistry encounters this fragment name.
     */
    private DirectiveRegistry directiveRegistry;

    public ReplaceNode(String fragmentName) {
        this.fragmentName = fragmentName;
    }

    /**
     * Capture the DirectiveRegistry for use when compiling fragments at
     * render time.  No early fragment resolution here — the registry comes
     * from the Model, which is not available until render().
     */
    @Override
    public Node compile(DirectiveRegistry registry) {
        this.directiveRegistry = registry;
        return this;
    }

    @Override
    public void render(Model model, StringBuilder out) {

        // Fetch the per-page registry from the model
        FragmentRegistry registry = model.getFragments();
        if (registry == null) {
            return; // no fragments attached to this model — silent skip
        }

        // Resolve (and cache inside the FragmentRegistry) on first use
        Node fragment = registry.get(fragmentName, directiveRegistry);
        if (fragment == null) {
            return; // fragment name not found — silent skip
        }

        // FragmentRegistry.get() returns a compiled <root> wrapper.
        // Render only its children to avoid emitting <root>...</root>.
        if (fragment instanceof ElementNode) {
            ElementNode root = (ElementNode) fragment;
            for (int i = 0; i < root.children.size(); i++) {
                root.children.get(i).render(model, out);
            }
        } else {
            fragment.render(model, out);
        }
    }
}