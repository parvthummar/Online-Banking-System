package com.beko.DemoBank_v1.controllers;


import com.beko.DemoBank_v1.models.Account;
import com.beko.DemoBank_v1.models.PaymentHistory;
import com.beko.DemoBank_v1.models.TransactionHistory;
import com.beko.DemoBank_v1.models.User;
import com.beko.DemoBank_v1.repository.AccountRepository;
import com.beko.DemoBank_v1.repository.PaymentHistoryRepository;
import com.beko.DemoBank_v1.repository.TransactHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/app")
public class AppController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;

    @Autowired
    private TransactHistoryRepository transactHistoryRepository;

    User user;
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(HttpSession session){
        user = (User) session.getAttribute("user");

        int userId =Integer.parseInt(user.getUser_id());

        List<Account> getUserAccounts = accountRepository.getUserAccountsById(userId);

        BigDecimal totalAccountsBalance = accountRepository.getTotalBalance(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userAccounts", getUserAccounts); 
        response.put("totalBalance", totalAccountsBalance); 
        return ResponseEntity.ok(response);

    }

    @GetMapping("/payment_history")
    public ResponseEntity<?> getPaymentHistory(HttpSession session){
        user = (User) session.getAttribute("user");


        int userId =Integer.parseInt(user.getUser_id());

        List<PaymentHistory> userPaymentHistory = paymentHistoryRepository.getPaymentsRecordsById(userId);

        Map<String, List> response = new HashMap<>();
        response.put("payment_history", userPaymentHistory); 


        return ResponseEntity.ok(response);

    }

    @GetMapping("/transaction_history")
    public ResponseEntity<?> getTransactiontHistory(HttpSession session){
        user = (User) session.getAttribute("user");


        int userId =Integer.parseInt(user.getUser_id());

        List<TransactionHistory> userTransactionHistory = transactHistoryRepository.getTransactionRecordsById(userId);

        Map<String, List> response = new HashMap<>();
        response.put("transaction_history", userTransactionHistory); 


        return ResponseEntity.ok(response);

    }


    @PostMapping("/account_transaction_history")
    public ResponseEntity<?> getAccountTransactiontHistory(@RequestBody Map<String, String> requestMap,HttpSession session){
       
        String account_id = requestMap.get("account_id");
        int accountId = Integer.parseInt(account_id);

    
        List<TransactionHistory> accountTransactionHistory = transactHistoryRepository.getTransactionRecordsByAccountId(accountId);

        Map<String, List> response = new HashMap<>();
        response.put("transaction_history", accountTransactionHistory); 


        return ResponseEntity.ok(response);

    }



}



