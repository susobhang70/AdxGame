# AdxGame
Implementation of the AdxGame by Enrique Areyan Viqueira (Brown University)

## Variants
|Variants|Description|Module|GameServer|SampleAgent|
|--------|------|--|----------|-----------|
|One Day Variant| One Day One Ad Campaign|`adx.variants.onedaygame` | `adx.server.GameServer`| `adx.variants.onedaygame.SimpleOneDayAgent`|
|Two Day One Campaign Variant| One ad campaign spanning over two days| `adx.variants.twodaysgame` | `adx.variants.twodaysgame.TwoDaysOneCampaignGameServer` | `adx.variants.twodaysgame.SimpleTwoDaysOneCampaignAgent`|
|Two Day Two Campaigns Variant| One ad campaign for each day for two days| `adx.variants.twodaysgame` | `adx.variants.twodaysgame.TwoDaysTwoCampaignsGameServer` | `adx.variants.twodaysgame.SimpleTwoDaysTwoCampaignsAgent`|
|Thirty Day Thirty Campaigns Variant (Added myself)| Thirty ad campaigns for each day for thirty days| `adx.variants.thirtydaysgame` | `adx.variants.thirtydaysgame.ThirtyDaysThirtyCampaignsGameServer` | `adx.variants.thirtydaysgame.SimpleThirtyDaysThirtyCampaignsAgent`|

Now it also computes average Quality scores over all the played games at the end.
