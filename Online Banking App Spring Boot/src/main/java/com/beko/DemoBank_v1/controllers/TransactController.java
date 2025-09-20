package com.beko.DemoBank_v1.controllers;

import com.beko.DemoBank_v1.models.PaymentRequest;
import com.beko.DemoBank_v1.models.TransferRequest;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.repository.AccountRepository;
import com.beko.DemoBank_v1.repository.PaymentRepository;
import com.beko.DemoBank_v1.repository.TransactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/transact")
public class TransactController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactRepository transactRepository;

    User user;
    int user_id;
    double currentBalance;
    double newBalance;
    LocalDateTime currentDateTime = LocalDateTime.now();

    @PostMapping("/deposit")
    public ResponseEntity deposit(@RequestBody Map<String, String> requestMap, HttpSession session) {

        String depositAmount = requestMap.get("deposit_amount");
        String accountID = requestMap.get("account_id");

        if (depositAmount.isEmpty() || accountID.isEmpty()) {
            return ResponseEntity.badRequest().body("Deposit amount and account ID cannot be empty.");
        }

        user = (User) session.getAttribute("user");

        int acc_id = Integer.parseInt(accountID);
        user_id = Integer.parseInt(user.getUser_id());

        double depositAmountValue = Double.parseDouble(depositAmount);

        if (depositAmountValue == 0) {
            return ResponseEntity.badRequest().body("Deposit amount cannot be zero.");
        }

        currentBalance = accountRepository.getAccountBalance(user_id, acc_id);
        newBalance = currentBalance + depositAmountValue;

        accountRepository.changeAccountsBalanceById(newBalance, acc_id);

        transactRepository.logTransaction(acc_id, "deposit", depositAmountValue, "online", "success", "Deposit Transaction Successfull", currentDateTime);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Amount Deposited Successfully.");
        response.put("accounts", accountRepository.getUserAccountsById(user_id));

        return ResponseEntity.ok(response);

    }

    @PostMapping("/transfer")
    ResponseEntity transfer(@RequestBody TransferRequest request, HttpSession session) {

        String transfer_from = request.getSourceAccount();
        String transfer_to = request.getTargetAccount();
        String transfer_amount = request.getAmount();

        if (transfer_from.isEmpty() || transfer_to.isEmpty() || transfer_amount.isEmpty()) {
            return ResponseEntity.badRequest().body("The account transferring from and to along with the amount cannot be empty!");
        }

        int transferFromId = Integer.parseInt(transfer_from);
        int transferToId = Integer.parseInt(transfer_to);
        double transferAmount = Double.parseDouble(transfer_amount);

        if (transferFromId == transferToId) {
            return ResponseEntity.badRequest().body("Cannot Transfer Into The Same Account, Please select the appropriate account to perform transfer.");
        }

        if (transferAmount == 0) {
            return ResponseEntity.badRequest().body("Cannot Transfer an amount of 0 (Zero) value, please enter a value greater than.");
        }

        user = (User) session.getAttribute("user");

        user_id = Integer.parseInt(user.getUser_id());
        double currentBalanceOfAccountTransferringFrom = accountRepository.getAccountBalance(user_id, transferFromId);

        if (currentBalanceOfAccountTransferringFrom < transferAmount) {
            transactRepository.logTransaction(transferFromId, "transfer", transferAmount, "online", "failed", "Insufficient funds.", currentDateTime);
            return ResponseEntity.badRequest().body("You have insufficient Funds to perform this transfer.");
        }

        double currentBalanceOfAccountTransferringTo = accountRepository.getAccountBalance(user_id, transferToId);

        double newBalanceOfAccountTransferringFrom = currentBalanceOfAccountTransferringFrom - transferAmount;
        double newBalanceOfAccountTransferringTo = currentBalanceOfAccountTransferringTo + transferAmount;

        accountRepository.changeAccountsBalanceById(newBalanceOfAccountTransferringFrom, transferFromId);
        accountRepository.changeAccountsBalanceById(newBalanceOfAccountTransferringTo, transferToId);

        transactRepository.logTransaction(transferFromId, "Transfer", transferAmount, "online", "success", "Transfer Transaction Successfull", currentDateTime);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Transfer completed successfully.");
        response.put("accounts", accountRepository.getUserAccountsById(user_id));

        return ResponseEntity.ok(response);

    }

    @PostMapping("/withdraw")
    ResponseEntity transfer(@RequestBody Map<String, String> requestMap, HttpSession session) {

        String withdrawalAmount = requestMap.get("withdrawal_amount");
        String accountId = requestMap.get("account_id");

        if (withdrawalAmount.isEmpty() || accountId.isEmpty()) {
            return ResponseEntity.badRequest().body("Account withdrawing from and withdrawal amount cannot be empty!");
        }

        int account_id = Integer.parseInt(accountId);
        double withdrawal_amount = Double.parseDouble(withdrawalAmount);

        if (withdrawal_amount == 0) {
            return ResponseEntity.badRequest().body("Withdrawal amount cannot be 0 value.");
        }

        user = (User) session.getAttribute("user");

        user_id = Integer.parseInt(user.getUser_id());
        currentBalance = accountRepository.getAccountBalance(user_id, account_id);

        if (currentBalance < withdrawal_amount) {
            transactRepository.logTransaction(account_id, "withdrawal", withdrawal_amount, "online", "failed", "Insufficient funds.", currentDateTime);
            return ResponseEntity.badRequest().body("You have insufficient Funds to perform this transfer.");
        }

        double newBalance = currentBalance - withdrawal_amount;

        accountRepository.changeAccountsBalanceById(newBalance, account_id);

        transactRepository.logTransaction(account_id, "Withdrawal", withdrawal_amount, "online", "success", "Withdrawal Transaction Successfull", currentDateTime);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Withdrawal Successfull!");
        response.put("accounts", accountRepository.getUserAccountsById(user_id));

        return ResponseEntity.ok(response);

    }

    @PostMapping("/payment")
    ResponseEntity transfer(@RequestBody PaymentRequest request, HttpSession session) {

        String beneficiary = request.getBeneficiary();
        String account_number = request.getAccount_number();
        String account_id = request.getAccount_id();
        String reference = request.getReference();
        String payment_amount = request.getPayment_amount();

        if (beneficiary.isEmpty() || account_number.isEmpty() || account_id.isEmpty() || payment_amount.isEmpty()) {
            return ResponseEntity.badRequest().body("Beneficiary, account number, account paying from and account payment amount cannot be empty.");
        }

        int accountID = Integer.parseInt(account_id);
        double paymentAmount = Double.parseDouble(payment_amount);

        if (paymentAmount == 0) {
            return ResponseEntity.badRequest().body("Payment amount cannot be 0.");
        }

        user = (User) session.getAttribute("user");

        user_id = Integer.parseInt(user.getUser_id());
        currentBalance = accountRepository.getAccountBalance(user_id, accountID);

        if (currentBalance < paymentAmount) {
            String reasonCode = "Coult not Processed Payment due to insufficient funds.";
            paymentRepository.makePayment(accountID, beneficiary, account_number, paymentAmount, reference, "failed", reasonCode, currentDateTime);
            transactRepository.logTransaction(accountID, "Payment", paymentAmount, "online", "failed", "Insufficient funds.", currentDateTime);
            return ResponseEntity.badRequest().body("You have insufficient Funds to perform this payment.");
        }

        newBalance = currentBalance - paymentAmount;

        accountRepository.changeAccountsBalanceById(newBalance, accountID);

        String reasonCode = "Payment Processed Successfully!";

        paymentRepository.makePayment(accountID, beneficiary, account_number, paymentAmount, reference, "success", reasonCode, currentDateTime);

        transactRepository.logTransaction(accountID, "Payment", paymentAmount, "online", "success", "Payment Transaction Successfull", currentDateTime);

        Map<String, Object> response = new HashMap<>();
        response.put("message", reasonCode);
        response.put("accounts", accountRepository.getUserAccountsById(user_id));

        return ResponseEntity.ok(response);

    }

}
