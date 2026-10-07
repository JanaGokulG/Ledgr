package com.ledgr.sandbox.ui;

import com.ledgr.sandbox.LedgrAaSandboxApplication;
import com.ledgr.sandbox.entity.AccountEntity;
import com.ledgr.sandbox.entity.TransactionEntity;
import com.ledgr.sandbox.repository.AccountRepository;
import com.ledgr.sandbox.repository.TransactionRepository;
import com.ledgr.sandbox.service.FiuIngestionService;
import com.ledgr.sandbox.service.RealtimeTransactionSimulatorService;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LedgrDesktopApp extends Application {

    private ConfigurableApplicationContext springContext;
    private final ExecutorService backgroundWorker = Executors.newVirtualThreadPerTaskExecutor();

    private FiuIngestionService fiuIngestionService;
    private RealtimeTransactionSimulatorService realtimeSimulatorService;
    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;

    // UI state
    private volatile boolean privacyShieldActive = false;
    private volatile boolean isLiveStreamActive = false;
    private final NumberFormat inrFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    // Dynamic UI elements
    private Label lblLiquidBalance;
    private Label lblLockedAssets;
    private Label lblTotalNetWorth;
    private Label lblScholarshipRunway;
    private Label lblPrivacyStatus;

    // Pre-allocated Account Card UI Components (Zero Garbage Collection churn)
    private static class AccountCardUi {
        VBox cardBox;
        Label lblType;
        Label lblNum;
        Label lblBal;
        Label lblDetails;
        String rawNumber = "";
        BigDecimal currentBal = BigDecimal.ZERO;
    }
    private final List<AccountCardUi> accountCards = new ArrayList<>();
    private HBox accountsRow;

    // Progress Bar & Buttons
    private ProgressBar syncProgressBar;
    private Label lblSyncStep;
    private Label lblSyncDetail;
    private Button btnSync;
    private Button btnLiveStream;
    private Button btnInstantTxn;
    private TextField txtSearch;

    // Toast Alert
    private HBox toastBox;
    private Label lblToastText;

    // Table & High-Performance UI Models
    public record TransactionRow(String id, String date, String payee, String category, String mode, 
                                 BigDecimal rawAmount, BigDecimal rawBalance, boolean isDebit, 
                                 String formattedAmount, String formattedBalance, String maskedAmount, String maskedBalance) {}

    private TableView<TransactionRow> transactionsTable;
    private final ObservableList<TransactionRow> masterTxnList = FXCollections.observableArrayList();
    private FilteredList<TransactionRow> filteredTxnList;

    @Override
    public void init() {
        // Fast Spring Boot start in-process
        springContext = new SpringApplicationBuilder(LedgrAaSandboxApplication.class)
                .web(org.springframework.boot.WebApplicationType.NONE)
                .headless(false)
                .run();

        fiuIngestionService = springContext.getBean(FiuIngestionService.class);
        realtimeSimulatorService = springContext.getBean(RealtimeTransactionSimulatorService.class);
        accountRepository = springContext.getBean(AccountRepository.class);
        transactionRepository = springContext.getBean(TransactionRepository.class);

        // Register async real-time event listener
        realtimeSimulatorService.registerListener(this::onNewRealtimeTransaction);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Ledgr – High Performance Personal Finance OS (RBI AA Framework)");

        StackPane rootStack = new StackPane();
        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("root");

        mainLayout.setLeft(buildSidebar());
        mainLayout.setCenter(buildMainContent());

        toastBox = buildToastBox();
        StackPane.setAlignment(toastBox, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(toastBox, new Insets(0, 30, 30, 0));

        rootStack.getChildren().addAll(mainLayout, toastBox);

        Scene scene = new Scene(rootStack, 1280, 820);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles/ledgr-dark.css").toExternalForm());
        } catch (Exception ignored) {}

        // Global Privacy Hotkey Shield (Ctrl + Shift + H)
        KeyCombination privacyCombination = new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
        scene.setOnKeyPressed(event -> {
            if (privacyCombination.match(event)) {
                togglePrivacyShield();
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();

        // Perform initial async data loading without blocking UI
        asyncLoadDashboardData(false);
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(18);
        sidebar.setPrefWidth(240);
        sidebar.getStyleClass().add("sidebar");

        HBox brand = new HBox(12);
        brand.setAlignment(Pos.CENTER_LEFT);
        
        StackPane logo = new StackPane();
        Circle bgCircle = new Circle(18, Color.web("#6366f1"));
        Label lText = new Label("L");
        lText.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        logo.getChildren().addAll(bgCircle, lText);

        VBox brandText = new VBox(2);
        Label title = new Label("LEDGR");
        title.getStyleClass().add("brand-title");
        Label sub = new Label("Student FinTech OS");
        sub.getStyleClass().add("brand-subtitle");
        brandText.getChildren().addAll(title, sub);
        brand.getChildren().addAll(logo, brandText);

        // Single User Profile Card
        VBox userCard = new VBox(3);
        userCard.setStyle("-fx-background-color: rgba(99, 102, 241, 0.08); -fx-padding: 10 12; -fx-background-radius: 8; -fx-border-color: rgba(99, 102, 241, 0.25); -fx-border-radius: 8;");
        Label lblUserName = new Label("👤 Akshay Kumar");
        lblUserName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #ffffff;");
        Label lblUserMeta = new Label("PAN: AIMPN5746M • KYC: Verified");
        lblUserMeta.setStyle("-fx-font-size: 10px; -fx-text-fill: #34d399;");
        Label lblUserVpa = new Label("Handle: akshay@setuaa");
        lblUserVpa.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        userCard.getChildren().addAll(lblUserName, lblUserMeta, lblUserVpa);

        VBox nav = new VBox(8);
        Button btnDash = createNavButton("📊  Dashboard & AA Sync", true);
        Button btnLedger = createNavButton("📜  Transaction Stream", false);
        Button btnEscrow = createNavButton("🤝  Roommate Escrow", false);
        Button btnRunway = createNavButton("🎓  Scholarship Runway", false);
        nav.getChildren().addAll(btnDash, btnLedger, btnEscrow, btnRunway);

        VBox hotkeyBox = new VBox(6);
        hotkeyBox.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-padding: 12; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 8;");
        Label hkTitle = new Label("🛡️ Privacy Shield");
        hkTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #e2e8f0;");
        lblPrivacyStatus = new Label("Status: VISIBLE");
        lblPrivacyStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #34d399;");
        Label hkHint = new Label("Hotkey: Ctrl+Shift+H");
        hkHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
        hotkeyBox.getChildren().addAll(hkTitle, lblPrivacyStatus, hkHint);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(brand, userCard, new Separator(), nav, spacer, hotkeyBox);
        return sidebar;
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add(active ? "nav-button-active" : "nav-button");
        return btn;
    }

    private VBox buildMainContent() {
        VBox content = new VBox(16);
        content.getStyleClass().add("content-area");

        // Top Action Bar
        HBox topBar = new HBox(12);
        topBar.setAlignment(Pos.CENTER_LEFT);

        VBox headerText = new VBox(2);
        Label headerTitle = new Label("RBI Account Aggregator – Live Financial Stream");
        headerTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label headerSub = new Label("Zero-lag local statement processing & real-time personal ledger");
        headerSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
        headerText.getChildren().addAll(headerTitle, headerSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnSync = new Button("⚡ Sync AA Gateway");
        btnSync.getStyleClass().add("btn-primary");
        btnSync.setOnAction(e -> runSteppedAaSync());

        btnInstantTxn = new Button("💸 Instant Txn");
        btnInstantTxn.getStyleClass().add("btn-secondary");
        btnInstantTxn.setOnAction(e -> generateInstantTransaction());

        btnLiveStream = new Button("🔴 Stream: OFF");
        btnLiveStream.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15); -fx-text-fill: #f87171; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-color: rgba(239, 68, 68, 0.3); -fx-border-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;");
        btnLiveStream.setOnAction(e -> toggleLiveStream());

        topBar.getChildren().addAll(headerText, spacer, btnSync, btnInstantTxn, btnLiveStream);

        // AA Progress Stepper Card
        VBox syncProgressCard = buildSyncProgressCard();

        // Metrics Row
        HBox metricsGrid = new HBox(14);
        lblLiquidBalance = new Label("₹0.00");
        lblLockedAssets = new Label("₹0.00");
        lblTotalNetWorth = new Label("₹0.00");
        lblScholarshipRunway = new Label("0.0 Mo");

        metricsGrid.getChildren().addAll(
                createMetricCard("Liquid Savings", lblLiquidBalance, "Savings (Instant Spend)"),
                createMetricCard("FD / RD Assets", lblLockedAssets, "Fixed & Recurring Deposits"),
                createMetricCard("Total Net Worth", lblTotalNetWorth, "Aggregated Student Assets"),
                createMetricCard("Scholarship Runway", lblScholarshipRunway, "Safe Buffer at Current Burn")
        );

        // Pre-initialized Accounts Container
        Label accSectionTitle = new Label("Linked Bank Accounts (3)");
        accSectionTitle.getStyleClass().add("card-title");
        accountsRow = new HBox(12);

        // Search Bar & Filter Strip
        HBox filterStrip = new HBox(12);
        filterStrip.setAlignment(Pos.CENTER_LEFT);

        Label txnSectionTitle = new Label("Transaction Stream");
        txnSectionTitle.getStyleClass().add("card-title");

        Region tableSpacer = new Region();
        HBox.setHgrow(tableSpacer, Priority.ALWAYS);

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Filter by Merchant, UPI, Category...");
        txtSearch.setPrefWidth(260);
        txtSearch.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: white; -fx-prompt-text-fill: #64748b; -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8; -fx-padding: 6 10;");
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applySearchFilter(newVal));

        filterStrip.getChildren().addAll(txnSectionTitle, tableSpacer, txtSearch);

        transactionsTable = buildOptimizedTransactionsTable();
        VBox.setVgrow(transactionsTable, Priority.ALWAYS);

        content.getChildren().addAll(topBar, syncProgressCard, metricsGrid, accSectionTitle, accountsRow, filterStrip, transactionsTable);
        return content;
    }

    private VBox buildSyncProgressCard() {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: rgba(15, 23, 42, 0.7); -fx-padding: 10 14; -fx-background-radius: 10; -fx-border-color: rgba(99, 102, 241, 0.2); -fx-border-radius: 10;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        lblSyncStep = new Label("AA Gateway Status: Idle");
        lblSyncStep.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: #a5b4fc;");

        lblSyncDetail = new Label("Local PostgreSQL cache connected.");
        lblSyncDetail.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        header.getChildren().addAll(lblSyncStep, new Label("•"), lblSyncDetail);

        syncProgressBar = new ProgressBar(0.0);
        syncProgressBar.setMaxWidth(Double.MAX_VALUE);
        syncProgressBar.setPrefHeight(4);
        syncProgressBar.setStyle("-fx-accent: #6366f1;");

        card.getChildren().addAll(header, syncProgressBar);
        return card;
    }

    private HBox buildToastBox() {
        HBox toast = new HBox(10);
        toast.setAlignment(Pos.CENTER_LEFT);
        toast.setStyle("-fx-background-color: rgba(15, 23, 42, 0.95); -fx-padding: 12 18; -fx-background-radius: 12; -fx-border-color: rgba(16, 185, 129, 0.5); -fx-border-radius: 12;");
        toast.setOpacity(0);
        toast.setMouseTransparent(true);

        lblToastText = new Label("⚡ Transaction Received");
        lblToastText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #34d399;");

        toast.getChildren().add(lblToastText);
        return toast;
    }

    private void showToast(String message, boolean isCredit) {
        Platform.runLater(() -> {
            lblToastText.setText(message);
            toastBox.setStyle(isCredit 
                    ? "-fx-background-color: rgba(15, 23, 42, 0.95); -fx-padding: 10 16; -fx-background-radius: 10; -fx-border-color: rgba(16, 185, 129, 0.5); -fx-border-radius: 10;"
                    : "-fx-background-color: rgba(15, 23, 42, 0.95); -fx-padding: 10 16; -fx-background-radius: 10; -fx-border-color: rgba(244, 63, 94, 0.5); -fx-border-radius: 10;");
            lblToastText.setStyle(isCredit ? "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #34d399;" : "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #fb7185;");

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toastBox);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            PauseTransition pause = new PauseTransition(Duration.seconds(2.5));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastBox);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeIn.setOnFinished(e -> pause.play());
            pause.setOnFinished(e -> fadeOut.play());
            fadeIn.play();
        });
    }

    private VBox createMetricCard(String title, Label valueLabel, String subtitle) {
        VBox card = new VBox(4);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lblTitle = new Label(title.toUpperCase());
        lblTitle.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8; -fx-font-weight: bold;");
        valueLabel.getStyleClass().add("metric-number");
        Label lblSub = new Label(subtitle);
        lblSub.getStyleClass().add("metric-sub");

        card.getChildren().addAll(lblTitle, valueLabel, lblSub);
        return card;
    }

    private TableView<TransactionRow> buildOptimizedTransactionsTable() {
        TableView<TransactionRow> table = new TableView<>();
        table.getStyleClass().add("table-view");

        filteredTxnList = new FilteredList<>(masterTxnList, p -> true);
        table.setItems(filteredTxnList);

        TableColumn<TransactionRow, String> colDate = new TableColumn<>("TIMESTAMP");
        colDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().date()));
        colDate.setPrefWidth(160);

        TableColumn<TransactionRow, String> colPayee = new TableColumn<>("MERCHANT / PAYEE");
        colPayee.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().payee()));
        colPayee.setPrefWidth(240);

        TableColumn<TransactionRow, String> colCategory = new TableColumn<>("CATEGORY");
        colCategory.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().category()));
        colCategory.setPrefWidth(180);

        TableColumn<TransactionRow, String> colMode = new TableColumn<>("MODE");
        colMode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().mode()));
        colMode.setPrefWidth(80);

        TableColumn<TransactionRow, String> colAmount = new TableColumn<>("AMOUNT (INR)");
        colAmount.setCellValueFactory(d -> new SimpleStringProperty(privacyShieldActive ? d.getValue().maskedAmount() : d.getValue().formattedAmount()));
        colAmount.setPrefWidth(130);

        TableColumn<TransactionRow, String> colBalance = new TableColumn<>("BALANCE");
        colBalance.setCellValueFactory(d -> new SimpleStringProperty(privacyShieldActive ? d.getValue().maskedBalance() : d.getValue().formattedBalance()));
        colBalance.setPrefWidth(130);

        table.getColumns().addAll(colDate, colPayee, colCategory, colMode, colAmount, colBalance);
        return table;
    }

    private void applySearchFilter(String query) {
        if (filteredTxnList == null) return;
        if (query == null || query.isBlank()) {
            filteredTxnList.setPredicate(p -> true);
            return;
        }
        String lower = query.toLowerCase().trim();
        filteredTxnList.setPredicate(row -> 
            row.payee().toLowerCase().contains(lower) || 
            row.category().toLowerCase().contains(lower) || 
            row.mode().toLowerCase().contains(lower) ||
            row.formattedAmount().contains(lower)
        );
    }

    private void asyncLoadDashboardData(boolean showToastWhenDone) {
        backgroundWorker.submit(() -> {
            try {
                List<AccountEntity> accounts = accountRepository.findAll();
                List<TransactionEntity> transactions = transactionRepository.findAll();

                BigDecimal totalLiquid = BigDecimal.ZERO;
                BigDecimal totalFd = BigDecimal.ZERO;
                BigDecimal totalRd = BigDecimal.ZERO;

                List<TransactionRow> rows = new ArrayList<>(transactions.size());

                for (AccountEntity acc : accounts) {
                    BigDecimal bal = acc.getCurrentBalance() != null ? acc.getCurrentBalance() : BigDecimal.ZERO;
                    String type = acc.getFiType() != null ? acc.getFiType() : "SAVINGS";

                    if ("DEPOSIT".equalsIgnoreCase(type) || "SAVINGS".equalsIgnoreCase(acc.getAccountType())) {
                        totalLiquid = totalLiquid.add(bal);
                    } else if ("TERM-DEPOSIT".equalsIgnoreCase(type) || "FIXED".equalsIgnoreCase(acc.getAccountType())) {
                        totalFd = totalFd.add(acc.getMaturityAmount() != null ? acc.getMaturityAmount() : bal);
                    } else if ("RECURRING_DEPOSIT".equalsIgnoreCase(type) || "RECURRING".equalsIgnoreCase(acc.getAccountType())) {
                        totalRd = totalRd.add(acc.getMaturityAmount() != null ? acc.getMaturityAmount() : bal);
                    }
                }

                // Pre-compute formatted string rows off the UI thread
                for (TransactionEntity txn : transactions) {
                    rows.add(convertEntityToRow(txn));
                }

                BigDecimal netWorth = totalLiquid.add(totalFd).add(totalRd);
                BigDecimal totalDebits = transactionRepository.getTotalDebits();
                if (totalDebits == null) totalDebits = BigDecimal.ZERO;

                BigDecimal monthlyBurn = totalDebits.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
                BigDecimal runway = monthlyBurn.compareTo(BigDecimal.ZERO) > 0
                        ? totalLiquid.divide(monthlyBurn, 1, RoundingMode.HALF_UP)
                        : BigDecimal.valueOf(999);

                final BigDecimal finalLiquid = totalLiquid;
                final BigDecimal finalLocked = totalFd.add(totalRd);
                final BigDecimal finalNetWorth = netWorth;
                final String runwayText = runway + " Months";

                Platform.runLater(() -> {
                    updateMetricsUi(finalLiquid, finalLocked, finalNetWorth, runwayText);
                    updateAccountCardsUi(accounts);
                    masterTxnList.setAll(rows);
                    if (showToastWhenDone) {
                        showToast("✅ Dashboard synchronized instantly", true);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private TransactionRow convertEntityToRow(TransactionEntity txn) {
        String dt = txn.getTransactionTimestamp() != null ? txn.getTransactionTimestamp().replace("T", " ") : "";
        String payee = txn.getMerchantPayee() != null ? txn.getMerchantPayee() : txn.getNarration();
        String cat = txn.getCategory() != null ? txn.getCategory() : "GENERAL";
        String mode = txn.getPaymentMode() != null ? txn.getPaymentMode() : "UPI";
        BigDecimal amt = txn.getAmount() != null ? txn.getAmount() : BigDecimal.ZERO;
        BigDecimal bal = txn.getBalanceAfterTxn() != null ? txn.getBalanceAfterTxn() : BigDecimal.ZERO;
        boolean isDebit = !"CREDIT".equalsIgnoreCase(txn.getTxnType());

        String prefix = isDebit ? "- " : "+ ";
        String formattedAmt = prefix + inrFormat.format(amt);
        String formattedBal = inrFormat.format(bal);

        return new TransactionRow(txn.getId(), dt, payee, cat, mode, amt, bal, isDebit, 
                formattedAmt, formattedBal, "₹•••", "₹•••,•••.••");
    }

    private void updateMetricsUi(BigDecimal liquid, BigDecimal locked, BigDecimal netWorth, String runway) {
        if (privacyShieldActive) {
            lblLiquidBalance.setText("₹•••,•••.••");
            lblLockedAssets.setText("₹•••,•••.••");
            lblTotalNetWorth.setText("₹•••,•••.••");
            lblScholarshipRunway.setText("•• Mo");
        } else {
            lblLiquidBalance.setText(inrFormat.format(liquid));
            lblLockedAssets.setText(inrFormat.format(locked));
            lblTotalNetWorth.setText(inrFormat.format(netWorth));
            lblScholarshipRunway.setText(runway);
        }
    }

    private void updateAccountCardsUi(List<AccountEntity> accounts) {
        // Initialize cards once or update existing in place
        if (accountCards.size() != accounts.size()) {
            accountCards.clear();
            accountsRow.getChildren().clear();

            for (AccountEntity acc : accounts) {
                AccountCardUi card = new AccountCardUi();
                card.rawNumber = acc.getAccountNumber();
                card.currentBal = acc.getCurrentBalance() != null ? acc.getCurrentBalance() : BigDecimal.ZERO;

                card.cardBox = new VBox(4);
                card.cardBox.getStyleClass().add("card");
                HBox.setHgrow(card.cardBox, Priority.ALWAYS);

                card.lblType = new Label(acc.getFiType() != null ? acc.getFiType() : "SAVINGS");
                card.lblType.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #38bdf8;");

                card.lblNum = new Label();
                card.lblNum.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");

                card.lblBal = new Label();
                card.lblBal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #34d399;");

                card.lblDetails = new Label((acc.getBranchName() != null ? acc.getBranchName() : "Main Branch") + " | " + acc.getHolderName());
                card.lblDetails.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

                card.cardBox.getChildren().addAll(card.lblType, card.lblNum, card.lblBal, card.lblDetails);
                accountCards.add(card);
                accountsRow.getChildren().add(card.cardBox);
            }
        }

        // Fast property updates
        for (int i = 0; i < accounts.size() && i < accountCards.size(); i++) {
            AccountEntity acc = accounts.get(i);
            AccountCardUi card = accountCards.get(i);
            card.currentBal = acc.getCurrentBalance() != null ? acc.getCurrentBalance() : BigDecimal.ZERO;

            card.lblNum.setText(privacyShieldActive ? "XXXXXXXX••••" : acc.getAccountNumber());
            card.lblBal.setText(privacyShieldActive ? "₹•••,•••.••" : inrFormat.format(card.currentBal));
        }
    }

    private void runSteppedAaSync() {
        btnSync.setDisable(true);
        backgroundWorker.submit(() -> {
            try {
                fiuIngestionService.executeFullSyncWithRealisticDelay("student@setuaa", (currentStep, totalSteps, stepTitle, message) -> {
                    Platform.runLater(() -> {
                        double prog = (double) currentStep / (double) totalSteps;
                        syncProgressBar.setProgress(prog);
                        lblSyncStep.setText(stepTitle);
                        lblSyncDetail.setText(message);
                    });
                });

                Platform.runLater(() -> {
                    syncProgressBar.setProgress(1.0);
                    lblSyncStep.setText("✅ Handshake Completed");
                    lblSyncDetail.setText("138 ReBIT transactions decrypted & persisted.");
                    btnSync.setDisable(false);
                    asyncLoadDashboardData(true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblSyncStep.setText("❌ Sync Failed");
                    lblSyncDetail.setText(e.getMessage());
                    btnSync.setDisable(false);
                });
            }
        });
    }

    private void generateInstantTransaction() {
        backgroundWorker.submit(() -> {
            try {
                realtimeSimulatorService.generateAndPersistTransaction(null);
            } catch (Exception e) {
                Platform.runLater(() -> showToast("Error: " + e.getMessage(), false));
            }
        });
    }

    private void toggleLiveStream() {
        isLiveStreamActive = !isLiveStreamActive;
        if (isLiveStreamActive) {
            realtimeSimulatorService.startLiveStreaming(3);
            btnLiveStream.setText("🟢 Stream: LIVE (3s)");
            btnLiveStream.setStyle("-fx-background-color: rgba(16, 185, 129, 0.18); -fx-text-fill: #34d399; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-color: rgba(16, 185, 129, 0.4); -fx-border-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;");
            showToast("🚀 Live Real-Time Stream Activated (3s interval)", true);
        } else {
            realtimeSimulatorService.stopLiveStreaming();
            btnLiveStream.setText("🔴 Stream: OFF");
            btnLiveStream.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15); -fx-text-fill: #f87171; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-color: rgba(239, 68, 68, 0.3); -fx-border-radius: 8; -fx-padding: 8 14; -fx-cursor: hand;");
        }
    }

    private void onNewRealtimeTransaction(TransactionEntity txn) {
        TransactionRow newRow = convertEntityToRow(txn);
        Platform.runLater(() -> {
            masterTxnList.add(0, newRow);
            
            // Fast incremental update without re-fetching all tables
            if (!accountCards.isEmpty()) {
                AccountCardUi savingsCard = accountCards.get(0);
                savingsCard.currentBal = txn.getBalanceAfterTxn();
                savingsCard.lblBal.setText(privacyShieldActive ? "₹•••,•••.••" : inrFormat.format(savingsCard.currentBal));
                if (!privacyShieldActive) {
                    lblLiquidBalance.setText(inrFormat.format(savingsCard.currentBal));
                }
            }

            boolean isCredit = !newRow.isDebit();
            String prefix = isCredit ? "+₹" : "-₹";
            showToast(String.format("⚡ %s: %s%s | Bal: %s", newRow.payee(), prefix, txn.getAmount(), inrFormat.format(newRow.rawBalance())), isCredit);
        });
    }

    private void togglePrivacyShield() {
        privacyShieldActive = !privacyShieldActive;
        lblPrivacyStatus.setText(privacyShieldActive ? "Status: SHIELDED (HIDDEN)" : "Status: VISIBLE");
        lblPrivacyStatus.setStyle(privacyShieldActive ? "-fx-font-size: 11px; -fx-text-fill: #f43f5e;" : "-fx-font-size: 11px; -fx-text-fill: #34d399;");
        
        // Fast in-place UI re-render without querying database
        asyncLoadDashboardData(false);
    }

    @Override
    public void stop() {
        if (realtimeSimulatorService != null) {
            realtimeSimulatorService.stopLiveStreaming();
        }
        backgroundWorker.shutdownNow();
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }
}
