package pro.sachin.fity.sercives.impl;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pro.sachin.fity.model.Payments;
import pro.sachin.fity.repository.PyamentRepository;
import pro.sachin.fity.sercives.PaymentService;

@RequiredArgsConstructor
@Service
public class PaymentServiceImpl implements PaymentService {
    
    private final PyamentRepository paymentRepository;

    @Override
    public void savePayment(Payments payments) {
        paymentRepository.save(payments);
    }
}
