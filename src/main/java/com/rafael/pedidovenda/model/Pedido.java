package com.rafael.pedidovenda.model;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;
import static javax.persistence.TemporalType.DATE;
import static javax.persistence.TemporalType.TIMESTAMP;
import static lombok.AccessLevel.NONE;
import static java.math.BigDecimal.ZERO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@Entity
@Table(name = "pedido")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pedido implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@NotNull
	@CreationTimestamp
	@Temporal(TIMESTAMP)
	@Column(name = "data_criacao", nullable = false, columnDefinition = "datetime")
	private Date dataCriacao;

	@Column(columnDefinition = "text")
	private String observacao;

	@Getter(value = NONE)
	@Temporal(DATE)
	@Column(name = "data_entrega", nullable = false)
	private Date dataEntrega;

	@Getter(value = NONE)
	@Column(name = "valor_frete", nullable = false, precision = 10, scale = 2)
	private BigDecimal valorFrete = ZERO;

	@Getter(value = NONE)
	@Column(name = "valor_desconto", nullable = false, precision = 10, scale = 2)
	private BigDecimal valorDesconto = ZERO;

	@Getter(value = NONE)
	@Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
	private BigDecimal valorTotal = ZERO;

	@NotNull
	@Enumerated(STRING)
	@Column(nullable = false, length = 20)
	private StatusPedido status = StatusPedido.ORCAMENTO;

	@Getter(value = NONE)
	@Enumerated(STRING)
	@Column(name = "forma_pagamento", nullable = false, length = 20)
	private FormaPagamento formaPagamento;

	@Getter(value = NONE)
	@ManyToOne
	@JoinColumn(nullable = false)
	private Usuario vendedor;

	@Getter(value = NONE)
	@ManyToOne
	@JoinColumn(nullable = false)
	private Cliente cliente;

	@Embedded
	private EnderecoEntrega enderecoEntrega;

	@Getter(NONE)
	@OneToMany(mappedBy = "pedido", cascade = ALL, orphanRemoval = true)
	private List<ItemPedido> itens = new ArrayList<>();
	
	@NotNull
	public BigDecimal getValorFrete() {
		return valorFrete;
	}

	@NotNull
	public BigDecimal getValorDesconto() {
		return valorDesconto;
	}
	
	@NotNull
	public BigDecimal getValorTotal() {
		return valorTotal;
	}
	
	@NotNull
	public Usuario getVendedor() {
		return vendedor;
	}
	
	@NotNull
	public Cliente getCliente() {
		return cliente;
	}
	
	@NotNull
	public FormaPagamento getFormaPagamento() {
		return formaPagamento;
	}
	
	@NotNull
	public Date getDataEntrega() {
		return dataEntrega;
	}
	
	@Transient
	public boolean isNovo() {
		return this.id == null;
	}
	
	@Transient
	public boolean isExistente() {
		return !isNovo();
	}
	
	@Transient
	public BigDecimal getValorSubtotal() {
		return this.getValorTotal()
				.subtract(this.getValorFrete())
				.add(this.getValorDesconto());
	}

	public void recalcularValorTotal() {
		BigDecimal total = BigDecimal.ZERO;
		total = total.add(this.getValorFrete().subtract(this.getValorDesconto()));
		
		for(ItemPedido item : this.getItens()) {
			if (item.getProduto() != null && item.getProduto().getId() != null) {
				total = total.add(item.getValorTotal());
			}
		}
		
		this.setValorTotal(total);
	}

	public void adicionarItemVazio() {
		if (isOrcamento()) {
			Produto produto = new Produto();

			ItemPedido item = new ItemPedido();
			item.setProduto(produto);
			item.setPedido(this);

			this.getItens().add(0, item);
		}
	}

	public void removerItemVazio() {
		ItemPedido primeiroItemDaLista = getItens().get(0);
		if (primeiroItemDaLista != null && 
				primeiroItemDaLista.getProduto().getId() == null) {
			getItens().remove(0);
		} 
	}
	
	@Transient
	public boolean isOrcamento() {
		return StatusPedido.ORCAMENTO.equals(this.getStatus());
	}
	
	@Transient
	public boolean isValorTotalNegativo() {
		return getValorTotal().compareTo(ZERO) < 0;
	}
	
	@Transient
	public boolean isEmitido() {
		return StatusPedido.EMITIDO.equals(status);
	}
	
	@Transient
	public boolean isNaoEmissivel() {
		return !isEmissivel();
	}
	
	@Transient
	private boolean isEmissivel() {
		return isExistente() && isOrcamento();
	}

	@Transient
	private boolean isCancelavel() {
		return isExistente() && !isCancelado();
	}
	
	@Transient
	public boolean isCancelado() {
		return StatusPedido.CANCELADO.equals(status);
	}
	
	@Transient
	public boolean isNaoCancelavel() {
		return !isCancelavel();
	}

	@Transient
	private boolean isAlteravel() {
		return isOrcamento();
	}
	
	@Transient
	public boolean isNaoAlteravel() {
		return !isAlteravel();
	}
	
	@Transient
	public boolean isNaoEnviavelPorEmail() {
		return isNovo() || isCancelado();
	}
	
	@Transient
	public boolean isNaoEnviavelPorSms() {
		return isNovo() || isCancelado();
	}
	
	@Transient
	public boolean isNaoEnviavelPorWhatsapp() {
		return isNovo() || isCancelado();
	}
	
	public List<ItemPedido> getItens() {
		return itens;
	}

}