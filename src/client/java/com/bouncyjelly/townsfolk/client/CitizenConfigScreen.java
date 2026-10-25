package com.bouncyjelly.townsfolk.client;

import com.bouncyjelly.townsfolk.citizen.CitizenConfigData;
import com.bouncyjelly.townsfolk.config.ClockTime;
import com.bouncyjelly.townsfolk.config.ConfigContext;
import com.bouncyjelly.townsfolk.config.ConfigOptions;
import com.bouncyjelly.townsfolk.network.SaveConfigPayload;
import com.bouncyjelly.townsfolk.schedule.Activity;
import com.bouncyjelly.townsfolk.schedule.Schedule;
import com.bouncyjelly.townsfolk.schedule.ScheduleEntry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.*;
import java.util.function.Consumer;

public class CitizenConfigScreen extends Screen {

    private static final int FIELD_W = 260;
    private static final int GRAY = 0xA0A0A0;

    private record LabelAt(String text, int x, int y) {}

    private static final class Row {
        String start, end;
        Activity activity;
        Row(String start, String end, Activity activity) {
            this.start = start; this.end = end; this.activity = activity;
        }
    }

    private final int entityId;
    private final ConfigContext context;
    
    private String name;
    private int skin;
    private String age_group;
    private String personality;
    private String home_id;
    private String work_id;
    private String wallet;
    private final Map<String, String> settings;
    private boolean customSchedule;
    private final List<Row> rows = new ArrayList<>();

    private final List<LabelAt> labels = new ArrayList<>();
    private ConfigOptions.Tab tab = ConfigOptions.Tab.GENERAL;
    private String footer = "";
    private int footerColor = GRAY;

    public CitizenConfigScreen(int entityId, CitizenConfigData data, ConfigContext context) {
        super(Text.literal("Configure Citizen"));
        this.entityId = entityId;
        this.context = context;
        this.name = data.name();
        this.skin = data.skin();
        this.age_group = data.age_group();
        this.personality = data.personality();
        this.home_id = data.home_id().orElse("");
        this.work_id = data.work_id().orElse("");
        this.wallet = Integer.toString(data.wallet());
        this.settings = new HashMap<>(data.settings());
        if (data.scheduleOverride().isPresent()) {
            customSchedule = true;
            loadRows(data.scheduleOverride().get());
        }
    }

    @Override
    public boolean shouldPause() { return false; }

    private void loadRows(Schedule s) {
        rows.clear();
        for (ScheduleEntry e : s.entries()) {
            rows.add(new Row(ClockTime.format(e.start()), ClockTime.format(e.end()), e.activity()));
        }
    }

    @Override
    protected void init() {
        labels.clear();
        ConfigOptions.Tab[] tabs = ConfigOptions.Tab.values();
        int tabW = Math.min(84, (width - 20) / tabs.length);
        int tx = width / 2 - (tabW * tabs.length) / 2;
        for (ConfigOptions.Tab t : tabs) {
            ButtonWidget b = ButtonWidget.builder(Text.literal(t.label), btn -> { tab = t; clearAndInit(); })
                    .dimensions(tx, 8, tabW, 20).build();
            b.active = t != tab;
            addDrawableChild(b);
            tx += tabW;
        }

        int x = width / 2 - FIELD_W / 2;
        switch (tab) {
            case GENERAL -> initGeneral(x, 50);
            case PLACES -> {}
            case SCHEDULE -> {}
            case PERSONALITY -> {}
            case SOCIAL -> {}
            case LIFE -> {}
            case STATUS -> {}
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Save"), b -> save())
                .dimensions(width / 2 - 102, height - 28, 100, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Close"), b -> close())
                .dimensions(width / 2 + 2, height - 28, 100, 20).build());
    }

    // ---- widget helpers ----
    private CyclingButtonWidget<String> cycle(String label, List<String> values, String current,
                                              int x, int y, Consumer<String> onChange) {
        String initial = values.contains(current) ? current : values.get(0);
        if (!values.contains(current)) onChange.accept(initial);
        return CyclingButtonWidget.<String>builder(v -> Text.literal(ConfigOptions.pretty(v)))
                .values(values)
                .initially(initial)
                .build(x, y, FIELD_W, 20, Text.literal(label), (btn, v) -> onChange.accept(v));
    }

    private int textField(String label, int x, int y, String initial, int maxLen,
                          String regex, Consumer<String> onChange) {
        labels.add(new LabelAt(label, x, y));
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y + 11, FIELD_W, 20, Text.literal(label));
        f.setMaxLength(maxLen);
        f.setText(initial);
        f.setTextPredicate(s -> s.matches(regex));
        f.setChangedListener(onChange);
        addDrawableChild(f);
        return y + 34;
    }

    private int drawWrapped(DrawContext ctx, String text, int x, int y, int w, int color) {
        for (OrderedText line : textRenderer.wrapLines(Text.literal(text), w)) {
            ctx.drawTextWithShadow(textRenderer, line, x, y, color);
            y += 10;
        }
        return y;
    }

    // ---- tabs ----
    private void initGeneral(int x, int y) {
        y = textField("Name", x, y, name, 24, "[A-Za-z0-9_-]*", s -> name = s);
        addDrawableChild(cycle("Age group (planned)", ConfigOptions.AGE_GROUPS, age_group, x, y, v -> age_group = v));
        y += 24;
        List<String> skins = new ArrayList<>();
        for (int i = 0; i < ConfigOptions.SKIN_VARIANTS; i++) skins.add(Integer.toString(i));
        addDrawableChild(cycle("Skin variant (planned)", skins, Integer.toString(skin), x, y, v -> skin = Integer.parseInt(v)));
        y += 24;
        textField("Wallet (stored, not used yet)", x, y, wallet, 7, "\\d*", s -> wallet = s);
    }

    // ---- saving ----
    private void fail(String message) {
        footer = message;
        footerColor = 0xFF5555;
    }

    public void onResult(boolean ok, String message) {
        footer = message;
        footerColor = ok ? 0x55FF55 : 0xFF5555;
    }

    private void save() {
        if (name.isEmpty()) { fail("Name can't be empty."); return; }

        int walletValue = 0;
        if (!wallet.isEmpty()) {
            try { walletValue = Integer.parseInt(wallet); }
            catch (NumberFormatException e) { fail("Wallet must be a number."); return; }
        }
        if (walletValue > 1_000_000) { fail("Wallet can be at most 1,000,000."); return; }

        Optional<Schedule> schedule = Optional.empty();
        if (customSchedule) {
            if (rows.isEmpty()) { fail("Add a schedule entry, or switch back to the default."); return; }
            List<ScheduleEntry> entries = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                Row r = rows.get(i);
                int s = ClockTime.parse(r.start);
                int e = ClockTime.parse(r.end);
                if (s < 0 || e < 0) { fail("Schedule row " + (i + 1) + ": use times like 07:30."); return; }
                if (e == 0) e = 24000;  // "06:00" as an end time means the end of the day
                entries.add(new ScheduleEntry(s, e, r.activity));
            }
            Schedule built = new Schedule(entries);
            List<String> problems = built.problems();
            if (!problems.isEmpty()) { fail("Schedule: " + problems.get(0)); return; }
            schedule = Optional.of(built);
        }

        CitizenConfigData data = new CitizenConfigData(name, skin, age_group, personality,
                home_id.isEmpty() ? Optional.empty() : Optional.of(home_id),
                work_id.isEmpty() ? Optional.empty() : Optional.of(work_id),
                walletValue, schedule, Map.copyOf(settings));
        ClientPlayNetworking.send(new SaveConfigPayload(entityId, data));
        footer = "Saving...";
        footerColor = GRAY;
    }

    // ---- drawing ----
    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        int x = width / 2 - FIELD_W / 2;

        for (LabelAt l : labels) ctx.drawTextWithShadow(textRenderer, l.text(), l.x(), l.y(), GRAY);

        if (tab == ConfigOptions.Tab.PERSONALITY || tab == ConfigOptions.Tab.SOCIAL || tab == ConfigOptions.Tab.LIFE) {
            drawWrapped(ctx, "Planned: saved, but doesn't affect citizens yet.", x, 34, FIELD_W, 0xFFC857);
        }

        ctx.drawCenteredTextWithShadow(textRenderer, footer, width / 2, height - 42, footerColor);
    }
}