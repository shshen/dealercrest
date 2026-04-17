# dealersystem

Web site builder:
1, Inline editing, clicking directly on an element in the page preview and editing just that field, rather than going through the config panel.
2, Block-level editing, for example:
User clicks the "Headline" field in the right panel -> the <h1> in the preview highlights with a blue outline. User types → preview updates live. User clicks "Background Image" -> the <section> with the background-image style highlights. Clean, intuitive, zero inline editing complexity.


Page Lifecycle:
ACTIVE    -> dealer's /specials page is live, indexed by Google
ARCHIVED  -> dealer deletes it, redirect record created
DELETED   -> hard delete after N days (optional cleanup)

On archive, your system:
Reads the Vehicle List block's saved filter config from the page
Reconstructs the canonical inventory URL from those filters
Stores the redirect mapping


// Vehicle List block config saved on the page
{
  condition: "used",
  make:      "toyota",
  model:     "camry",
  yearMin:   2020,
  priceMax:  30000
}

// Reconstructed canonical URL
/inventory/used/toyota/camry?yearMin=2020&priceMax=30000


Candidate domain names:
dealerbase, dealersoft, dealersystem, dealerforce, dealerhub,dealerstack,dealergrid,dealerpilot, dealersite
dealerworld,dealerhome