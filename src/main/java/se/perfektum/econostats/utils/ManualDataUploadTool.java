package se.perfektum.econostats.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import se.perfektum.econostats.EconoStatsController;
import se.perfektum.econostats.bank.CsvReader;
import se.perfektum.econostats.bank.nordea.NordeaCsvReader;
import se.perfektum.econostats.dao.AccountTransactionDao;
import se.perfektum.econostats.dao.googledrive.ConvenientUtils;
import se.perfektum.econostats.dao.googledrive.GoogleDriveDao;
import se.perfektum.econostats.domain.AccountTransaction;
import se.perfektum.econostats.domain.PayeeFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Simple tool for uploading/resetting data on Google Drive. Mainly for testing purposes.
 * <p>
 * NOTE: recurringTransactions file will NOT be restored!
 */
public class ManualDataUploadTool {

    private static EconoStatsController econoStatsController;

    private final static String localPayeeFilters = "c:\\EconoStats\\payeeFilters.json";
    private final static String localAccountTransactions = "c:/EconoStats/nordeaGemensamt.csv";

    static private AccountTransactionDao dao = new GoogleDriveDao();
    static private CsvReader csvReader = new NordeaCsvReader();
    static private ConvenientUtils convenientUtils = new ConvenientUtils(dao);

    public static void main(String args[]) {
        ApplicationContext context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
        econoStatsController = (EconoStatsController) context.getBean("econoStatsController");

        List<PayeeFilter> payeeFilters = savePayeeFiltersToDrive();
        List<AccountTransaction> accountTransactions = saveAccountTransactionsToDrive();
        // Pass accountTransaction=null when doing a full reset
        // Passing a list of accountTransactions here will merge it will an existing list on Drive!
        generateRecurringTransactions(payeeFilters, null);
    }

    private static List<AccountTransaction> saveAccountTransactionsToDrive() {
        List<AccountTransaction> transactions = null;
        try {
            transactions = csvReader.getAccountTransactionsFromFile(localAccountTransactions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        convenientUtils.saveJsonItemsToDrive(transactions, "transactions", "accountTransactions");
        return transactions;
    }

    private static List<PayeeFilter> savePayeeFiltersToDrive() {
        String pFilters = null;
        try {
            pFilters = new String(Files.readAllBytes(Paths.get(localPayeeFilters)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<PayeeFilter> payeeFilters = JsonUtils.getJsonElement(PayeeFilter.class, pFilters);
        convenientUtils.saveJsonItemsToDrive(payeeFilters, "payeeFilters", "payeeFilters");
        return payeeFilters;
    }

    private static void generateRecurringTransactions(List<PayeeFilter> payeeFilters, List<AccountTransaction> accountTransactions) {
        try {
            if (payeeFilters != null) {
                econoStatsController.generateRecurringTransactions(payeeFilters, accountTransactions);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
