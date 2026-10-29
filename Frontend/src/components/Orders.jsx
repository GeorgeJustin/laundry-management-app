
import { useEffect, useMemo, useState } from 'react'
import './Orders.css'

const API_BASE_URL = 'http://localhost:8081/api'

function formatDate(date) {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
}

function getDateOffset(days) {
    const date = new Date()
    date.setDate(date.getDate() + days)
    return formatDate(date)
}

function formatMoney(value) {
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        maximumFractionDigits: 2,
    }).format(Number(value ?? 0))
}

function formatSlot(slot) {
    const start = String(slot.startTime ?? '').slice(0, 5)
    const end = String(slot.endTime ?? '').slice(0, 5)
    return `${start} - ${end}`
}

function formatDateTime(value) {
    if (!value) return 'Not available'
    return new Date(value).toLocaleString('en-IN', {
        dateStyle: 'medium',
        timeStyle: 'short',
    })
}

async function readResponse(response) {
    const data = await response.json().catch(() => null)

    if (!response.ok) {
        const message =
            data?.message ||
            data?.detail ||
            data?.error ||
            `Request failed (${response.status})`

        throw new Error(message)
    }

    return data
}

export default function Orders({ services = [] }) {
    const [cart, setCart] = useState({})
    const [customerName, setCustomerName] = useState('')
    const [customerPhone, setCustomerPhone] = useState('')
    const [pickupAddress, setPickupAddress] = useState('')
    const [deliveryAddress, setDeliveryAddress] = useState('')
    const [customerNotes, setCustomerNotes] = useState('')

    const [pickupDate, setPickupDate] = useState(getDateOffset(1))
    const [deliveryDate, setDeliveryDate] = useState(getDateOffset(2))
    const [pickupSlots, setPickupSlots] = useState([])
    const [deliverySlots, setDeliverySlots] = useState([])
    const [pickupTime, setPickupTime] = useState('')
    const [deliveryTime, setDeliveryTime] = useState('')
    const [pickupSlotsLoading, setPickupSlotsLoading] = useState(false)
    const [deliverySlotsLoading, setDeliverySlotsLoading] = useState(false)

    const [slotsError, setSlotsError] = useState('')
    const [submitting, setSubmitting] = useState(false)
    const [pageError, setPageError] = useState('')
    const [createdOrder, setCreatedOrder] = useState(null)

    const [lookupNumber, setLookupNumber] = useState('')
    const [lookedUpOrder, setLookedUpOrder] = useState(null)
    const [lookupLoading, setLookupLoading] = useState(false)
    const [lookupError, setLookupError] = useState('')

    const availableServices = useMemo(
        () => services.filter((service) => service.active !== false),
        [services],
    )

    const cartItems = useMemo(
        () =>
            availableServices
                .filter((service) => Number(cart[service.id] ?? 0) > 0)
                .map((service) => ({
                    service,
                    quantity: Number(cart[service.id]),
                })),
        [availableServices, cart],
    )

    const estimatedSubtotal = useMemo(
        () =>
            cartItems.reduce(
                (total, item) =>
                    total + Number(item.service.price ?? 0) * item.quantity,
                0,
            ),
        [cartItems],
    )

    function updateQuantity(serviceId, value) {
        const quantity = Number(value)

        setCart((current) => {
            const updated = { ...current }

            if (!Number.isFinite(quantity) || quantity <= 0) {
                delete updated[serviceId]
            } else {
                updated[serviceId] = quantity
            }

            return updated
        })
    }

    useEffect(() => {
        let cancelled = false

        async function loadSlots() {
            setPickupSlotsLoading(true)
            setSlotsError('')
            setPickupSlots([])
            setPickupTime('')

            try {
                const query = new URLSearchParams({
                    date: pickupDate,
                    type: 'PICKUP',
                })

                const response = await fetch(
                    `${API_BASE_URL}/orders/available-slots?${query}`,
                )
                const data = await readResponse(response)

                if (!cancelled) {
                    const available = Array.isArray(data)
                        ? data.filter((slot) => slot.available)
                        : []

                    setPickupSlots(available)
                }
            } catch (error) {
                if (!cancelled) {
                    setSlotsError(
                        error instanceof Error
                            ? error.message
                            : 'Could not load pickup slots.',
                    )
                }
            } finally {
                if (!cancelled) setPickupSlotsLoading(false)
            }
        }

        if (pickupDate) loadSlots()

        return () => {
            cancelled = true
        }
    }, [pickupDate])

    useEffect(() => {
        let cancelled = false

        async function loadSlots() {
            setDeliverySlotsLoading(true)
            setSlotsError('')
            setDeliverySlots([])
            setDeliveryTime('')

            try {
                const query = new URLSearchParams({
                    date: deliveryDate,
                    type: 'DELIVERY',
                })

                const response = await fetch(
                    `${API_BASE_URL}/orders/available-slots?${query}`,
                )
                const data = await readResponse(response)

                if (!cancelled) {
                    const available = Array.isArray(data)
                        ? data.filter((slot) => slot.available)
                        : []

                    setDeliverySlots(available)
                }
            } catch (error) {
                if (!cancelled) {
                    setSlotsError(
                        error instanceof Error
                            ? error.message
                            : 'Could not load delivery slots.',
                    )
                }
            } finally {
                if (!cancelled) setDeliverySlotsLoading(false)
            }
        }

        if (deliveryDate) loadSlots()

        return () => {
            cancelled = true
        }
    }, [deliveryDate])

    function handlePickupDateChange(value) {
        setPickupDate(value)

        // Suggest delivery on the following day.
        if (value) {
            const nextDate = new Date(`${value}T12:00:00`)
            nextDate.setDate(nextDate.getDate() + 1)
            const suggestedDeliveryDate = formatDate(nextDate)

            if (deliveryDate <= value) {
                setDeliveryDate(suggestedDeliveryDate)
            }
        }
    }

    async function handleCreateOrder(event) {
        event.preventDefault()
        setPageError('')
        setCreatedOrder(null)

        if (cartItems.length === 0) {
            setPageError('Add at least one service to your cart.')
            return
        }

        if (!customerName.trim() || !customerPhone.trim()) {
            setPageError('Enter the customer name and phone number.')
            return
        }

        if (
            !pickupAddress.trim() ||
            !deliveryAddress.trim() ||
            !pickupTime ||
            !deliveryTime
        ) {
            setPageError(
                'Enter both addresses and select available pickup and delivery slots.',
            )
            return
        }

        if (deliveryDate <= pickupDate) {
            setPageError('Choose a delivery date after the pickup date.')
            return
        }

        const pickupSlot = pickupSlots.find(
            (slot) => slot.startTime === pickupTime,
        )
        const deliverySlot = deliverySlots.find(
            (slot) => slot.startTime === deliveryTime,
        )

        if (!pickupSlot || !deliverySlot) {
            setPageError('Please select currently available time slots.')
            return
        }

        const payload = {
            customerName: customerName.trim(),
            customerPhone: customerPhone.trim(),
            pickupAddress: pickupAddress.trim(),
            deliveryAddress: deliveryAddress.trim(),
            pickupScheduledAt: `${pickupDate}T${pickupTime}`,
            deliveryScheduledAt: `${deliveryDate}T${deliveryTime}`,
            customerNotes: customerNotes.trim(),
            items: cartItems.map(({ service, quantity }) => ({
                serviceId: service.id,
                quantity,
            })),
        }

        setSubmitting(true)

        try {
            const response = await fetch(`${API_BASE_URL}/orders`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            })

            const order = await readResponse(response)
            setCreatedOrder(order)
            setCart({})
            setLookedUpOrder(null)
            setLookupError('')
        } catch (error) {
            setPageError(
                error instanceof Error
                    ? error.message
                    : 'Unable to create the order. Please try again.',
            )
        } finally {
            setSubmitting(false)
        }
    }

    async function handleLookupOrder(event) {
        event.preventDefault()
        setLookupError('')
        setLookedUpOrder(null)

        const number = lookupNumber.trim()

        if (!number) {
            setLookupError('Enter an order number.')
            return
        }

        setLookupLoading(true)

        try {
            const response = await fetch(
                `${API_BASE_URL}/orders/${encodeURIComponent(number)}`,
            )
            const order = await readResponse(response)
            setLookedUpOrder(order)
        } catch (error) {
            setLookupError(
                error instanceof Error
                    ? error.message
                    : 'Unable to find that order.',
            )
        } finally {
            setLookupLoading(false)
        }
    }

    function renderOrderSummary(order) {
        return (
            <section className="order-result" aria-live="polite">
                <h3>Order details</h3>
                <p>
                    <strong>Order number:</strong> {order.orderNumber}
                </p>
                <p>
                    <strong>Status:</strong> {order.orderStatus}
                </p>
                <p>
                    <strong>Customer:</strong> {order.customerName}
                </p>
                <p>
                    <strong>Pickup:</strong> {formatDateTime(order.pickupScheduledAt)}
                </p>
                <p>
                    <strong>Delivery:</strong> {formatDateTime(order.deliveryScheduledAt)}
                </p>

                <div className="order-total-row">
                    <span>Estimated total</span>
                    <strong>{formatMoney(order.estimatedTotal)}</strong>
                </div>
                <div className="order-total-row">
                    <span>Delivery fee</span>
                    <strong>{formatMoney(order.deliveryFee)}</strong>
                </div>

                {order.finalTotal != null && (
                    <div className="order-total-row">
                        <span>Final total</span>
                        <strong>{formatMoney(order.finalTotal)}</strong>
                    </div>
                )}

                {order.items?.length > 0 && (
                    <>
                        <h4>Services in this order</h4>
                        <div className="order-table-wrap">
                            <table className="order-table">
                                <thead>
                                <tr>
                                    <th>Service</th>
                                    <th>Unit</th>
                                    <th>Quantity</th>
                                    <th>Estimated amount</th>
                                </tr>
                                </thead>
                                <tbody>
                                {order.items.map((item) => (
                                    <tr key={item.id}>
                                        <td>{item.serviceName}</td>
                                        <td>{item.pricingUnit}</td>
                                        <td>{item.quantity}</td>
                                        <td>{formatMoney(item.estimatedLineTotal)}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    </>
                )}
            </section>
        )
    }

    return (
        <div className="orders-page">
            <div className="orders-heading">
                <div>
                    <h1>Orders</h1>
                    <p>Book multiple laundry services under one order number.</p>
                </div>
            </div>

            {pageError && (
                <div className="order-alert order-alert-error" role="alert">
                    {pageError}
                </div>
            )}

            {createdOrder && (
                <div className="order-alert order-alert-success" role="status">
                    <h2>Order created successfully!</h2>
                    <p>
                        Your order number is <strong>{createdOrder.orderNumber}</strong>.
                        Keep it to look up your order later.
                    </p>
                    {renderOrderSummary(createdOrder)}
                </div>
            )}

            <section className="order-panel">
                <h2>1. Select services</h2>

                {availableServices.length === 0 ? (
                    <p>
                        No services are available. Check that the backend is running and
                        the Services page can load service data.
                    </p>
                ) : (
                    <div className="order-service-list">
                        {availableServices.map((service) => (
                            <div className="order-service-row" key={service.id}>
                                <div className="order-service-info">
                                    <strong>{service.name}</strong>
                                    <span>{service.description}</span>
                                    <small>
                                        {formatMoney(service.price)} / {service.pricingUnit || 'unit'}
                                    </small>
                                </div>

                                <label className="order-quantity">
                                    <span>Quantity</span>
                                    <input
                                        type="number"
                                        min="0"
                                        step="0.1"
                                        value={cart[service.id] ?? ''}
                                        onChange={(event) =>
                                            updateQuantity(service.id, event.target.value)
                                        }
                                        placeholder="0"
                                        aria-label={`Quantity for ${service.name}`}
                                    />
                                </label>
                            </div>
                        ))}
                    </div>
                )}

                <div className="order-total-row order-cart-total">
                    <span>Cart estimate before backend calculation</span>
                    <strong>{formatMoney(estimatedSubtotal)}</strong>
                </div>
                <p className="order-help">
                    This is only a preview. The backend calculates the official order
                    amount, delivery fee and final total.
                </p>
            </section>

            <form className="order-form" onSubmit={handleCreateOrder}>
                <section className="order-panel">
                    <h2>2. Customer details</h2>

                    <div className="order-form-grid">
                        <label>
                            Customer name *
                            <input
                                value={customerName}
                                onChange={(event) => setCustomerName(event.target.value)}
                                maxLength={150}
                                required
                            />
                        </label>

                        <label>
                            Phone number *
                            <input
                                type="tel"
                                value={customerPhone}
                                onChange={(event) => setCustomerPhone(event.target.value)}
                                maxLength={20}
                                required
                            />
                        </label>

                        <label className="order-full-width">
                            Pickup address *
                            <textarea
                                value={pickupAddress}
                                onChange={(event) => setPickupAddress(event.target.value)}
                                maxLength={500}
                                rows={2}
                                required
                            />
                        </label>

                        <label className="order-full-width">
                            Delivery address *
                            <textarea
                                value={deliveryAddress}
                                onChange={(event) => setDeliveryAddress(event.target.value)}
                                maxLength={500}
                                rows={2}
                                required
                            />
                        </label>

                        <label className="order-full-width">
                            Notes (optional)
                            <textarea
                                value={customerNotes}
                                onChange={(event) => setCustomerNotes(event.target.value)}
                                maxLength={1000}
                                rows={2}
                            />
                        </label>
                    </div>
                </section>

                <section className="order-panel">
                    <h2>3. Pickup schedule</h2>

                    <div className="order-form-grid">
                        <label>
                            Pickup date *
                            <input
                                type="date"
                                min={getDateOffset(0)}
                                value={pickupDate}
                                onChange={(event) =>
                                    handlePickupDateChange(event.target.value)
                                }
                                required
                            />
                        </label>

                        <label>
                            Available pickup slot *
                            <select
                                value={pickupTime}
                                onChange={(event) => setPickupTime(event.target.value)}
                                disabled={pickupSlotsLoading || pickupSlots.length === 0}
                                required
                            >
                                <option value="">
                                    {pickupSlotsLoading
                                        ? 'Loading slots...'
                                        : 'Select pickup slot'}
                                </option>
                                {pickupSlots.map((slot) => (
                                    <option key={slot.startTime} value={slot.startTime}>
                                        {formatSlot(slot)} ({slot.remainingCapacity} spaces left)
                                    </option>
                                ))}
                            </select>
                        </label>
                    </div>

                    {!pickupSlotsLoading && pickupSlots.length === 0 && (
                        <p className="order-help">
                            No pickup slots are available on this date. Choose another date.
                        </p>
                    )}
                </section>

                <section className="order-panel">
                    <h2>4. Delivery schedule</h2>

                    <div className="order-form-grid">
                        <label>
                            Delivery date *
                            <input
                                type="date"
                                min={pickupDate ? getDateOffset(0) : getDateOffset(1)}
                                value={deliveryDate}
                                onChange={(event) => setDeliveryDate(event.target.value)}
                                required
                            />
                        </label>

                        <label>
                            Available delivery slot *
                            <select
                                value={deliveryTime}
                                onChange={(event) => setDeliveryTime(event.target.value)}
                                disabled={deliverySlotsLoading || deliverySlots.length === 0}
                                required
                            >
                                <option value="">
                                    {deliverySlotsLoading
                                        ? 'Loading slots...'
                                        : 'Select delivery slot'}
                                </option>
                                {deliverySlots.map((slot) => (
                                    <option key={slot.startTime} value={slot.startTime}>
                                        {formatSlot(slot)} ({slot.remainingCapacity} spaces left)
                                    </option>
                                ))}
                            </select>
                        </label>
                    </div>

                    {!deliverySlotsLoading && deliverySlots.length === 0 && (
                        <p className="order-help">
                            No delivery slots are available on this date. Choose another date.
                        </p>
                    )}

                    {slotsError && (
                        <p className="order-alert order-alert-error" role="alert">
                            {slotsError}
                        </p>
                    )}
                </section>

                <section className="order-panel">
                    <h2>5. Review and book</h2>
                    <p>
                        Selected services: <strong>{cartItems.length}</strong>
                    </p>
                    <p>
                        Estimated item subtotal: <strong>{formatMoney(estimatedSubtotal)}</strong>
                    </p>
                    <p className="order-help">
                        The backend will calculate the order total. Payment method selection
                        is not included in the current create-order request.
                    </p>

                    <button
                        className="order-primary-button"
                        type="submit"
                        disabled={
                            submitting ||
                            pickupSlotsLoading ||
                            deliverySlotsLoading ||
                            !pickupTime ||
                            !deliveryTime
                        }
                    >
                        {submitting ? 'Creating order...' : 'Place order'}
                    </button>
                </section>
            </form>

            <section className="order-panel">
                <h2>Look up an existing order</h2>
                <form className="order-lookup-form" onSubmit={handleLookupOrder}>
                    <label>
                        Order number
                        <input
                            value={lookupNumber}
                            onChange={(event) => setLookupNumber(event.target.value)}
                            placeholder="Enter your order number"
                            required
                        />
                    </label>

                    <button
                        className="order-secondary-button"
                        type="submit"
                        disabled={lookupLoading}
                    >
                        {lookupLoading ? 'Searching...' : 'Find order'}
                    </button>
                </form>

                {lookupError && (
                    <p className="order-alert order-alert-error" role="alert">
                        {lookupError}
                    </p>
                )}

                {lookedUpOrder && renderOrderSummary(lookedUpOrder)}
            </section>
        </div>
    )
}
