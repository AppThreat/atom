const _: () = ::protobuf::__internal::assert_compatible_gencode_version("4.33.1-release");
// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__PropertyValue_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct PropertyValue {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<PropertyValue>
}

impl ::protobuf::Message for PropertyValue {}

impl ::std::default::Default for PropertyValue {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for PropertyValue {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `PropertyValue` is `Sync` because it does not implement interior mutability.
//    Neither does `PropertyValueMut`.
unsafe impl Sync for PropertyValue {}

// SAFETY:
// - `PropertyValue` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for PropertyValue {}

impl ::protobuf::Proxied for PropertyValue {
  type View<'msg> = PropertyValueView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for PropertyValue {}

impl ::protobuf::MutProxied for PropertyValue {
  type Mut<'msg> = PropertyValueMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct PropertyValueView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, PropertyValue>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for PropertyValueView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for PropertyValueView<'msg> {
  type Message = PropertyValue;
}

impl ::std::fmt::Debug for PropertyValueView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for PropertyValueView<'_> {
  fn default() -> PropertyValueView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, PropertyValue>> for PropertyValueView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, PropertyValue>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> PropertyValueView<'msg> {

  pub fn to_owned(&self) -> PropertyValue {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // string_value: optional string
  pub fn has_string_value(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(0)
    }
  }
  pub fn string_value_opt(self) -> ::protobuf::Optional<&'msg ::protobuf::ProtoStr> {
        ::protobuf::Optional::new(self.string_value(), self.has_string_value())
  }
  pub fn string_value(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // bool_value: optional bool
  pub fn has_bool_value(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn bool_value_opt(self) -> ::protobuf::Optional<bool> {
        ::protobuf::Optional::new(self.bool_value(), self.has_bool_value())
  }
  pub fn bool_value(self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        1, (false).into()
      ).try_into().unwrap()
    }
  }

  // int_value: optional int32
  pub fn has_int_value(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(2)
    }
  }
  pub fn int_value_opt(self) -> ::protobuf::Optional<i32> {
        ::protobuf::Optional::new(self.int_value(), self.has_int_value())
  }
  pub fn int_value(self) -> i32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (0i32).into()
      ).try_into().unwrap()
    }
  }

  // long_value: optional int64
  pub fn has_long_value(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(3)
    }
  }
  pub fn long_value_opt(self) -> ::protobuf::Optional<i64> {
        ::protobuf::Optional::new(self.long_value(), self.has_long_value())
  }
  pub fn long_value(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        3, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // float_value: optional float
  pub fn has_float_value(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(4)
    }
  }
  pub fn float_value_opt(self) -> ::protobuf::Optional<f32> {
        ::protobuf::Optional::new(self.float_value(), self.has_float_value())
  }
  pub fn float_value(self) -> f32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_f32_at_index(
        4, (0f32).into()
      ).try_into().unwrap()
    }
  }

  // double_value: optional double
  pub fn has_double_value(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(5)
    }
  }
  pub fn double_value_opt(self) -> ::protobuf::Optional<f64> {
        ::protobuf::Optional::new(self.double_value(), self.has_double_value())
  }
  pub fn double_value(self) -> f64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_f64_at_index(
        5, (0f64).into()
      ).try_into().unwrap()
    }
  }

  // string_list: optional message atom.StringList
  pub fn has_string_list(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(6)
    }
  }
  pub fn string_list_opt(self) -> ::protobuf::Optional<super::StringListView<'msg>> {
        ::protobuf::Optional::new(self.string_list(), self.has_string_list())
  }
  pub fn string_list(self) -> super::StringListView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(6)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::StringListView::default())
  }

  // bool_list: optional message atom.BoolList
  pub fn has_bool_list(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(7)
    }
  }
  pub fn bool_list_opt(self) -> ::protobuf::Optional<super::BoolListView<'msg>> {
        ::protobuf::Optional::new(self.bool_list(), self.has_bool_list())
  }
  pub fn bool_list(self) -> super::BoolListView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(7)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::BoolListView::default())
  }

  // int_list: optional message atom.IntList
  pub fn has_int_list(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(8)
    }
  }
  pub fn int_list_opt(self) -> ::protobuf::Optional<super::IntListView<'msg>> {
        ::protobuf::Optional::new(self.int_list(), self.has_int_list())
  }
  pub fn int_list(self) -> super::IntListView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(8)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::IntListView::default())
  }

  // long_list: optional message atom.LongList
  pub fn has_long_list(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(9)
    }
  }
  pub fn long_list_opt(self) -> ::protobuf::Optional<super::LongListView<'msg>> {
        ::protobuf::Optional::new(self.long_list(), self.has_long_list())
  }
  pub fn long_list(self) -> super::LongListView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(9)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::LongListView::default())
  }

  // float_list: optional message atom.FloatList
  pub fn has_float_list(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(10)
    }
  }
  pub fn float_list_opt(self) -> ::protobuf::Optional<super::FloatListView<'msg>> {
        ::protobuf::Optional::new(self.float_list(), self.has_float_list())
  }
  pub fn float_list(self) -> super::FloatListView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(10)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::FloatListView::default())
  }

  // double_list: optional message atom.DoubleList
  pub fn has_double_list(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(11)
    }
  }
  pub fn double_list_opt(self) -> ::protobuf::Optional<super::DoubleListView<'msg>> {
        ::protobuf::Optional::new(self.double_list(), self.has_double_list())
  }
  pub fn double_list(self) -> super::DoubleListView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(11)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::DoubleListView::default())
  }

  // contained_refs: optional message atom.ContainedRefs
  pub fn has_contained_refs(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(12)
    }
  }
  pub fn contained_refs_opt(self) -> ::protobuf::Optional<super::ContainedRefsView<'msg>> {
        ::protobuf::Optional::new(self.contained_refs(), self.has_contained_refs())
  }
  pub fn contained_refs(self) -> super::ContainedRefsView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(12)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::ContainedRefsView::default())
  }

  pub fn value(self) -> super::property_value::ValueOneof<'msg> {
    match self.value_case() {
      super::property_value::ValueCase::StringValue =>
          super::property_value::ValueOneof::StringValue(self.string_value()),
      super::property_value::ValueCase::BoolValue =>
          super::property_value::ValueOneof::BoolValue(self.bool_value()),
      super::property_value::ValueCase::IntValue =>
          super::property_value::ValueOneof::IntValue(self.int_value()),
      super::property_value::ValueCase::LongValue =>
          super::property_value::ValueOneof::LongValue(self.long_value()),
      super::property_value::ValueCase::FloatValue =>
          super::property_value::ValueOneof::FloatValue(self.float_value()),
      super::property_value::ValueCase::DoubleValue =>
          super::property_value::ValueOneof::DoubleValue(self.double_value()),
      super::property_value::ValueCase::StringList =>
          super::property_value::ValueOneof::StringList(self.string_list()),
      super::property_value::ValueCase::BoolList =>
          super::property_value::ValueOneof::BoolList(self.bool_list()),
      super::property_value::ValueCase::IntList =>
          super::property_value::ValueOneof::IntList(self.int_list()),
      super::property_value::ValueCase::LongList =>
          super::property_value::ValueOneof::LongList(self.long_list()),
      super::property_value::ValueCase::FloatList =>
          super::property_value::ValueOneof::FloatList(self.float_list()),
      super::property_value::ValueCase::DoubleList =>
          super::property_value::ValueOneof::DoubleList(self.double_list()),
      super::property_value::ValueCase::ContainedRefs =>
          super::property_value::ValueOneof::ContainedRefs(self.contained_refs()),
      _ => super::property_value::ValueOneof::not_set(std::marker::PhantomData)
    }
  }

  pub fn value_case(self) -> super::property_value::ValueCase {
    unsafe {
      let field_num = <Self as ::protobuf::__internal::runtime::UpbGetMessagePtr>::get_ptr(
          &self, ::protobuf::__internal::Private)
          .which_oneof_field_number_by_index(0);
      super::property_value::ValueCase::try_from(field_num).unwrap_unchecked()
    }
  }
}

// SAFETY:
// - `PropertyValueView` is `Sync` because it does not support mutation.
unsafe impl Sync for PropertyValueView<'_> {}

// SAFETY:
// - `PropertyValueView` is `Send` because while its alive a `PropertyValueMut` cannot.
// - `PropertyValueView` does not use thread-local data.
unsafe impl Send for PropertyValueView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for PropertyValueView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for PropertyValueView<'msg> {}

impl<'msg> ::protobuf::AsView for PropertyValueView<'msg> {
  type Proxied = PropertyValue;
  fn as_view(&self) -> ::protobuf::View<'msg, PropertyValue> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for PropertyValueView<'msg> {
  fn into_view<'shorter>(self) -> PropertyValueView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<PropertyValue> for PropertyValueView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> PropertyValue {
    let mut dst = PropertyValue::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<PropertyValue> for PropertyValueMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> PropertyValue {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for PropertyValue {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for PropertyValueView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for PropertyValueMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct PropertyValueMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, PropertyValue>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for PropertyValueMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for PropertyValueMut<'msg> {
  type Message = PropertyValue;
}

impl ::std::fmt::Debug for PropertyValueMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, PropertyValue>> for PropertyValueMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, PropertyValue>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> PropertyValueMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, PropertyValue> {
    self.inner
  }

  pub fn to_owned(&self) -> PropertyValue {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // string_value: optional string
  pub fn has_string_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(0)
    }
  }
  pub fn clear_string_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        0
      );
    }
  }
  pub fn string_value_opt(&self) -> ::protobuf::Optional<&'_ ::protobuf::ProtoStr> {
        ::protobuf::Optional::new(self.string_value(), self.has_string_value())
  }
  pub fn string_value(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_string_value(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // bool_value: optional bool
  pub fn has_bool_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_bool_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn bool_value_opt(&self) -> ::protobuf::Optional<bool> {
        ::protobuf::Optional::new(self.bool_value(), self.has_bool_value())
  }
  pub fn bool_value(&self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        1, (false).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_bool_value(&mut self, val: bool) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_bool_at_index(
        1, val.into()
      )
    }
  }

  // int_value: optional int32
  pub fn has_int_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(2)
    }
  }
  pub fn clear_int_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        2
      );
    }
  }
  pub fn int_value_opt(&self) -> ::protobuf::Optional<i32> {
        ::protobuf::Optional::new(self.int_value(), self.has_int_value())
  }
  pub fn int_value(&self) -> i32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (0i32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_int_value(&mut self, val: i32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        2, val.into()
      )
    }
  }

  // long_value: optional int64
  pub fn has_long_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(3)
    }
  }
  pub fn clear_long_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        3
      );
    }
  }
  pub fn long_value_opt(&self) -> ::protobuf::Optional<i64> {
        ::protobuf::Optional::new(self.long_value(), self.has_long_value())
  }
  pub fn long_value(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        3, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_long_value(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        3, val.into()
      )
    }
  }

  // float_value: optional float
  pub fn has_float_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(4)
    }
  }
  pub fn clear_float_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        4
      );
    }
  }
  pub fn float_value_opt(&self) -> ::protobuf::Optional<f32> {
        ::protobuf::Optional::new(self.float_value(), self.has_float_value())
  }
  pub fn float_value(&self) -> f32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_f32_at_index(
        4, (0f32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_float_value(&mut self, val: f32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_f32_at_index(
        4, val.into()
      )
    }
  }

  // double_value: optional double
  pub fn has_double_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(5)
    }
  }
  pub fn clear_double_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        5
      );
    }
  }
  pub fn double_value_opt(&self) -> ::protobuf::Optional<f64> {
        ::protobuf::Optional::new(self.double_value(), self.has_double_value())
  }
  pub fn double_value(&self) -> f64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_f64_at_index(
        5, (0f64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_double_value(&mut self, val: f64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_f64_at_index(
        5, val.into()
      )
    }
  }

  // string_list: optional message atom.StringList
  pub fn has_string_list(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(6)
    }
  }
  pub fn clear_string_list(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        6
      );
    }
  }
  pub fn string_list_opt(&self) -> ::protobuf::Optional<super::StringListView<'_>> {
        ::protobuf::Optional::new(self.string_list(), self.has_string_list())
  }
  pub fn string_list(&self) -> super::StringListView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(6)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::StringListView::default())
  }
  pub fn string_list_mut(&mut self) -> super::StringListMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         6, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_string_list(&mut self,
    val: impl ::protobuf::IntoProxied<super::StringList>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        6,
        val
      );
    }
  }

  // bool_list: optional message atom.BoolList
  pub fn has_bool_list(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(7)
    }
  }
  pub fn clear_bool_list(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        7
      );
    }
  }
  pub fn bool_list_opt(&self) -> ::protobuf::Optional<super::BoolListView<'_>> {
        ::protobuf::Optional::new(self.bool_list(), self.has_bool_list())
  }
  pub fn bool_list(&self) -> super::BoolListView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(7)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::BoolListView::default())
  }
  pub fn bool_list_mut(&mut self) -> super::BoolListMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         7, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_bool_list(&mut self,
    val: impl ::protobuf::IntoProxied<super::BoolList>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        7,
        val
      );
    }
  }

  // int_list: optional message atom.IntList
  pub fn has_int_list(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(8)
    }
  }
  pub fn clear_int_list(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        8
      );
    }
  }
  pub fn int_list_opt(&self) -> ::protobuf::Optional<super::IntListView<'_>> {
        ::protobuf::Optional::new(self.int_list(), self.has_int_list())
  }
  pub fn int_list(&self) -> super::IntListView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(8)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::IntListView::default())
  }
  pub fn int_list_mut(&mut self) -> super::IntListMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         8, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_int_list(&mut self,
    val: impl ::protobuf::IntoProxied<super::IntList>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        8,
        val
      );
    }
  }

  // long_list: optional message atom.LongList
  pub fn has_long_list(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(9)
    }
  }
  pub fn clear_long_list(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        9
      );
    }
  }
  pub fn long_list_opt(&self) -> ::protobuf::Optional<super::LongListView<'_>> {
        ::protobuf::Optional::new(self.long_list(), self.has_long_list())
  }
  pub fn long_list(&self) -> super::LongListView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(9)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::LongListView::default())
  }
  pub fn long_list_mut(&mut self) -> super::LongListMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         9, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_long_list(&mut self,
    val: impl ::protobuf::IntoProxied<super::LongList>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        9,
        val
      );
    }
  }

  // float_list: optional message atom.FloatList
  pub fn has_float_list(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(10)
    }
  }
  pub fn clear_float_list(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        10
      );
    }
  }
  pub fn float_list_opt(&self) -> ::protobuf::Optional<super::FloatListView<'_>> {
        ::protobuf::Optional::new(self.float_list(), self.has_float_list())
  }
  pub fn float_list(&self) -> super::FloatListView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(10)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::FloatListView::default())
  }
  pub fn float_list_mut(&mut self) -> super::FloatListMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         10, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_float_list(&mut self,
    val: impl ::protobuf::IntoProxied<super::FloatList>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        10,
        val
      );
    }
  }

  // double_list: optional message atom.DoubleList
  pub fn has_double_list(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(11)
    }
  }
  pub fn clear_double_list(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        11
      );
    }
  }
  pub fn double_list_opt(&self) -> ::protobuf::Optional<super::DoubleListView<'_>> {
        ::protobuf::Optional::new(self.double_list(), self.has_double_list())
  }
  pub fn double_list(&self) -> super::DoubleListView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(11)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::DoubleListView::default())
  }
  pub fn double_list_mut(&mut self) -> super::DoubleListMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         11, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_double_list(&mut self,
    val: impl ::protobuf::IntoProxied<super::DoubleList>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        11,
        val
      );
    }
  }

  // contained_refs: optional message atom.ContainedRefs
  pub fn has_contained_refs(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(12)
    }
  }
  pub fn clear_contained_refs(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        12
      );
    }
  }
  pub fn contained_refs_opt(&self) -> ::protobuf::Optional<super::ContainedRefsView<'_>> {
        ::protobuf::Optional::new(self.contained_refs(), self.has_contained_refs())
  }
  pub fn contained_refs(&self) -> super::ContainedRefsView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(12)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::ContainedRefsView::default())
  }
  pub fn contained_refs_mut(&mut self) -> super::ContainedRefsMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         12, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_contained_refs(&mut self,
    val: impl ::protobuf::IntoProxied<super::ContainedRefs>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        12,
        val
      );
    }
  }

  pub fn value(&self) -> super::property_value::ValueOneof<'_> {
    match &self.value_case() {
      super::property_value::ValueCase::StringValue =>
          super::property_value::ValueOneof::StringValue(self.string_value()),
      super::property_value::ValueCase::BoolValue =>
          super::property_value::ValueOneof::BoolValue(self.bool_value()),
      super::property_value::ValueCase::IntValue =>
          super::property_value::ValueOneof::IntValue(self.int_value()),
      super::property_value::ValueCase::LongValue =>
          super::property_value::ValueOneof::LongValue(self.long_value()),
      super::property_value::ValueCase::FloatValue =>
          super::property_value::ValueOneof::FloatValue(self.float_value()),
      super::property_value::ValueCase::DoubleValue =>
          super::property_value::ValueOneof::DoubleValue(self.double_value()),
      super::property_value::ValueCase::StringList =>
          super::property_value::ValueOneof::StringList(self.string_list()),
      super::property_value::ValueCase::BoolList =>
          super::property_value::ValueOneof::BoolList(self.bool_list()),
      super::property_value::ValueCase::IntList =>
          super::property_value::ValueOneof::IntList(self.int_list()),
      super::property_value::ValueCase::LongList =>
          super::property_value::ValueOneof::LongList(self.long_list()),
      super::property_value::ValueCase::FloatList =>
          super::property_value::ValueOneof::FloatList(self.float_list()),
      super::property_value::ValueCase::DoubleList =>
          super::property_value::ValueOneof::DoubleList(self.double_list()),
      super::property_value::ValueCase::ContainedRefs =>
          super::property_value::ValueOneof::ContainedRefs(self.contained_refs()),
      _ => super::property_value::ValueOneof::not_set(std::marker::PhantomData)
    }
  }

  pub fn value_case(&self) -> super::property_value::ValueCase {
    unsafe {
      let field_num = <Self as ::protobuf::__internal::runtime::UpbGetMessagePtr>::get_ptr(
          &self, ::protobuf::__internal::Private)
          .which_oneof_field_number_by_index(0);
      super::property_value::ValueCase::try_from(field_num).unwrap_unchecked()
    }
  }
}

// SAFETY:
// - `PropertyValueMut` does not perform any shared mutation.
unsafe impl Send for PropertyValueMut<'_> {}

// SAFETY:
// - `PropertyValueMut` does not perform any shared mutation.
unsafe impl Sync for PropertyValueMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for PropertyValueMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for PropertyValueMut<'msg> {}

impl<'msg> ::protobuf::AsView for PropertyValueMut<'msg> {
  type Proxied = PropertyValue;
  fn as_view(&self) -> ::protobuf::View<'_, PropertyValue> {
    PropertyValueView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for PropertyValueMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, PropertyValue>
  where
      'msg: 'shorter {
    PropertyValueView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for PropertyValueMut<'msg> {
  type MutProxied = PropertyValue;
  fn as_mut(&mut self) -> PropertyValueMut<'msg> {
    PropertyValueMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for PropertyValueMut<'msg> {
  fn into_mut<'shorter>(self) -> PropertyValueMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl PropertyValue {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, PropertyValue> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> PropertyValueView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> PropertyValueMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // string_value: optional string
  pub fn has_string_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(0)
    }
  }
  pub fn clear_string_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        0
      );
    }
  }
  pub fn string_value_opt(&self) -> ::protobuf::Optional<&'_ ::protobuf::ProtoStr> {
        ::protobuf::Optional::new(self.string_value(), self.has_string_value())
  }
  pub fn string_value(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_string_value(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // bool_value: optional bool
  pub fn has_bool_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_bool_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn bool_value_opt(&self) -> ::protobuf::Optional<bool> {
        ::protobuf::Optional::new(self.bool_value(), self.has_bool_value())
  }
  pub fn bool_value(&self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        1, (false).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_bool_value(&mut self, val: bool) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_bool_at_index(
        1, val.into()
      )
    }
  }

  // int_value: optional int32
  pub fn has_int_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(2)
    }
  }
  pub fn clear_int_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        2
      );
    }
  }
  pub fn int_value_opt(&self) -> ::protobuf::Optional<i32> {
        ::protobuf::Optional::new(self.int_value(), self.has_int_value())
  }
  pub fn int_value(&self) -> i32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (0i32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_int_value(&mut self, val: i32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        2, val.into()
      )
    }
  }

  // long_value: optional int64
  pub fn has_long_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(3)
    }
  }
  pub fn clear_long_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        3
      );
    }
  }
  pub fn long_value_opt(&self) -> ::protobuf::Optional<i64> {
        ::protobuf::Optional::new(self.long_value(), self.has_long_value())
  }
  pub fn long_value(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        3, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_long_value(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        3, val.into()
      )
    }
  }

  // float_value: optional float
  pub fn has_float_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(4)
    }
  }
  pub fn clear_float_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        4
      );
    }
  }
  pub fn float_value_opt(&self) -> ::protobuf::Optional<f32> {
        ::protobuf::Optional::new(self.float_value(), self.has_float_value())
  }
  pub fn float_value(&self) -> f32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_f32_at_index(
        4, (0f32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_float_value(&mut self, val: f32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_f32_at_index(
        4, val.into()
      )
    }
  }

  // double_value: optional double
  pub fn has_double_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(5)
    }
  }
  pub fn clear_double_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        5
      );
    }
  }
  pub fn double_value_opt(&self) -> ::protobuf::Optional<f64> {
        ::protobuf::Optional::new(self.double_value(), self.has_double_value())
  }
  pub fn double_value(&self) -> f64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_f64_at_index(
        5, (0f64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_double_value(&mut self, val: f64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_f64_at_index(
        5, val.into()
      )
    }
  }

  // string_list: optional message atom.StringList
  pub fn has_string_list(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(6)
    }
  }
  pub fn clear_string_list(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        6
      );
    }
  }
  pub fn string_list_opt(&self) -> ::protobuf::Optional<super::StringListView<'_>> {
        ::protobuf::Optional::new(self.string_list(), self.has_string_list())
  }
  pub fn string_list(&self) -> super::StringListView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(6)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::StringListView::default())
  }
  pub fn string_list_mut(&mut self) -> super::StringListMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         6, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_string_list(&mut self,
    val: impl ::protobuf::IntoProxied<super::StringList>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        6,
        val
      );
    }
  }

  // bool_list: optional message atom.BoolList
  pub fn has_bool_list(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(7)
    }
  }
  pub fn clear_bool_list(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        7
      );
    }
  }
  pub fn bool_list_opt(&self) -> ::protobuf::Optional<super::BoolListView<'_>> {
        ::protobuf::Optional::new(self.bool_list(), self.has_bool_list())
  }
  pub fn bool_list(&self) -> super::BoolListView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(7)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::BoolListView::default())
  }
  pub fn bool_list_mut(&mut self) -> super::BoolListMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         7, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_bool_list(&mut self,
    val: impl ::protobuf::IntoProxied<super::BoolList>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        7,
        val
      );
    }
  }

  // int_list: optional message atom.IntList
  pub fn has_int_list(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(8)
    }
  }
  pub fn clear_int_list(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        8
      );
    }
  }
  pub fn int_list_opt(&self) -> ::protobuf::Optional<super::IntListView<'_>> {
        ::protobuf::Optional::new(self.int_list(), self.has_int_list())
  }
  pub fn int_list(&self) -> super::IntListView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(8)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::IntListView::default())
  }
  pub fn int_list_mut(&mut self) -> super::IntListMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         8, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_int_list(&mut self,
    val: impl ::protobuf::IntoProxied<super::IntList>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        8,
        val
      );
    }
  }

  // long_list: optional message atom.LongList
  pub fn has_long_list(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(9)
    }
  }
  pub fn clear_long_list(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        9
      );
    }
  }
  pub fn long_list_opt(&self) -> ::protobuf::Optional<super::LongListView<'_>> {
        ::protobuf::Optional::new(self.long_list(), self.has_long_list())
  }
  pub fn long_list(&self) -> super::LongListView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(9)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::LongListView::default())
  }
  pub fn long_list_mut(&mut self) -> super::LongListMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         9, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_long_list(&mut self,
    val: impl ::protobuf::IntoProxied<super::LongList>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        9,
        val
      );
    }
  }

  // float_list: optional message atom.FloatList
  pub fn has_float_list(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(10)
    }
  }
  pub fn clear_float_list(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        10
      );
    }
  }
  pub fn float_list_opt(&self) -> ::protobuf::Optional<super::FloatListView<'_>> {
        ::protobuf::Optional::new(self.float_list(), self.has_float_list())
  }
  pub fn float_list(&self) -> super::FloatListView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(10)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::FloatListView::default())
  }
  pub fn float_list_mut(&mut self) -> super::FloatListMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         10, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_float_list(&mut self,
    val: impl ::protobuf::IntoProxied<super::FloatList>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        10,
        val
      );
    }
  }

  // double_list: optional message atom.DoubleList
  pub fn has_double_list(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(11)
    }
  }
  pub fn clear_double_list(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        11
      );
    }
  }
  pub fn double_list_opt(&self) -> ::protobuf::Optional<super::DoubleListView<'_>> {
        ::protobuf::Optional::new(self.double_list(), self.has_double_list())
  }
  pub fn double_list(&self) -> super::DoubleListView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(11)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::DoubleListView::default())
  }
  pub fn double_list_mut(&mut self) -> super::DoubleListMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         11, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_double_list(&mut self,
    val: impl ::protobuf::IntoProxied<super::DoubleList>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        11,
        val
      );
    }
  }

  // contained_refs: optional message atom.ContainedRefs
  pub fn has_contained_refs(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(12)
    }
  }
  pub fn clear_contained_refs(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        12
      );
    }
  }
  pub fn contained_refs_opt(&self) -> ::protobuf::Optional<super::ContainedRefsView<'_>> {
        ::protobuf::Optional::new(self.contained_refs(), self.has_contained_refs())
  }
  pub fn contained_refs(&self) -> super::ContainedRefsView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(12)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::ContainedRefsView::default())
  }
  pub fn contained_refs_mut(&mut self) -> super::ContainedRefsMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         12, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_contained_refs(&mut self,
    val: impl ::protobuf::IntoProxied<super::ContainedRefs>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        12,
        val
      );
    }
  }

  pub fn value(&self) -> super::property_value::ValueOneof<'_> {
    match &self.value_case() {
      super::property_value::ValueCase::StringValue =>
          super::property_value::ValueOneof::StringValue(self.string_value()),
      super::property_value::ValueCase::BoolValue =>
          super::property_value::ValueOneof::BoolValue(self.bool_value()),
      super::property_value::ValueCase::IntValue =>
          super::property_value::ValueOneof::IntValue(self.int_value()),
      super::property_value::ValueCase::LongValue =>
          super::property_value::ValueOneof::LongValue(self.long_value()),
      super::property_value::ValueCase::FloatValue =>
          super::property_value::ValueOneof::FloatValue(self.float_value()),
      super::property_value::ValueCase::DoubleValue =>
          super::property_value::ValueOneof::DoubleValue(self.double_value()),
      super::property_value::ValueCase::StringList =>
          super::property_value::ValueOneof::StringList(self.string_list()),
      super::property_value::ValueCase::BoolList =>
          super::property_value::ValueOneof::BoolList(self.bool_list()),
      super::property_value::ValueCase::IntList =>
          super::property_value::ValueOneof::IntList(self.int_list()),
      super::property_value::ValueCase::LongList =>
          super::property_value::ValueOneof::LongList(self.long_list()),
      super::property_value::ValueCase::FloatList =>
          super::property_value::ValueOneof::FloatList(self.float_list()),
      super::property_value::ValueCase::DoubleList =>
          super::property_value::ValueOneof::DoubleList(self.double_list()),
      super::property_value::ValueCase::ContainedRefs =>
          super::property_value::ValueOneof::ContainedRefs(self.contained_refs()),
      _ => super::property_value::ValueOneof::not_set(std::marker::PhantomData)
    }
  }

  pub fn value_case(&self) -> super::property_value::ValueCase {
    unsafe {
      let field_num = <Self as ::protobuf::__internal::runtime::UpbGetMessagePtr>::get_ptr(
          &self, ::protobuf::__internal::Private)
          .which_oneof_field_number_by_index(0);
      super::property_value::ValueCase::try_from(field_num).unwrap_unchecked()
    }
  }
}  // impl PropertyValue

impl ::std::ops::Drop for PropertyValue {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for PropertyValue {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for PropertyValue {
  type Proxied = Self;
  fn as_view(&self) -> PropertyValueView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for PropertyValue {
  type MutProxied = Self;
  fn as_mut(&mut self) -> PropertyValueMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for PropertyValue {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__PropertyValue_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$1T/(+! 3333333^!|#|$|%|&|(|)|*|+|,|-|.|/");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__PropertyValue_msg_init.0, &[<super::StringList as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::BoolList as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::IntList as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::LongList as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::FloatList as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::DoubleList as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::ContainedRefs as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__PropertyValue_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for PropertyValue {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for PropertyValue {
  type Msg = PropertyValue;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<PropertyValue> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for PropertyValue {
  type Msg = PropertyValue;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<PropertyValue> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for PropertyValueMut<'_> {
  type Msg = PropertyValue;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<PropertyValue> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for PropertyValueMut<'_> {
  type Msg = PropertyValue;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<PropertyValue> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for PropertyValueView<'_> {
  type Msg = PropertyValue;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<PropertyValue> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for PropertyValueMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

pub mod property_value {

#[non_exhaustive]
#[derive(Debug, Clone, Copy)]
#[allow(dead_code)]
#[repr(u32)]
pub enum ValueOneof<'msg> {
  StringValue(&'msg ::protobuf::ProtoStr) = 1,
  BoolValue(bool) = 2,
  IntValue(i32) = 3,
  LongValue(i64) = 4,
  FloatValue(f32) = 5,
  DoubleValue(f64) = 6,
  StringList(::protobuf::View<'msg, super::super::StringList>) = 7,
  BoolList(::protobuf::View<'msg, super::super::BoolList>) = 8,
  IntList(::protobuf::View<'msg, super::super::IntList>) = 9,
  LongList(::protobuf::View<'msg, super::super::LongList>) = 10,
  FloatList(::protobuf::View<'msg, super::super::FloatList>) = 11,
  DoubleList(::protobuf::View<'msg, super::super::DoubleList>) = 12,
  ContainedRefs(::protobuf::View<'msg, super::super::ContainedRefs>) = 13,

  not_set(std::marker::PhantomData<&'msg ()>) = 0
}
#[repr(C)]
#[derive(Debug, Copy, Clone, PartialEq, Eq)]
#[non_exhaustive]
#[allow(dead_code)]
pub enum ValueCase {
  StringValue = 1,
  BoolValue = 2,
  IntValue = 3,
  LongValue = 4,
  FloatValue = 5,
  DoubleValue = 6,
  StringList = 7,
  BoolList = 8,
  IntList = 9,
  LongList = 10,
  FloatList = 11,
  DoubleList = 12,
  ContainedRefs = 13,

  not_set = 0
}

impl ValueCase {
  #[allow(dead_code)]
  pub(crate) fn try_from(v: u32) -> ::std::option::Option<ValueCase> {
    match v {
      0 => Some(ValueCase::not_set),
      1 => Some(ValueCase::StringValue),
      2 => Some(ValueCase::BoolValue),
      3 => Some(ValueCase::IntValue),
      4 => Some(ValueCase::LongValue),
      5 => Some(ValueCase::FloatValue),
      6 => Some(ValueCase::DoubleValue),
      7 => Some(ValueCase::StringList),
      8 => Some(ValueCase::BoolList),
      9 => Some(ValueCase::IntList),
      10 => Some(ValueCase::LongList),
      11 => Some(ValueCase::FloatList),
      12 => Some(ValueCase::DoubleList),
      13 => Some(ValueCase::ContainedRefs),
      _ => None
    }
  }
}
}  // pub mod property_value


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__ContainedRefs_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct ContainedRefs {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<ContainedRefs>
}

impl ::protobuf::Message for ContainedRefs {}

impl ::std::default::Default for ContainedRefs {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for ContainedRefs {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `ContainedRefs` is `Sync` because it does not implement interior mutability.
//    Neither does `ContainedRefsMut`.
unsafe impl Sync for ContainedRefs {}

// SAFETY:
// - `ContainedRefs` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for ContainedRefs {}

impl ::protobuf::Proxied for ContainedRefs {
  type View<'msg> = ContainedRefsView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for ContainedRefs {}

impl ::protobuf::MutProxied for ContainedRefs {
  type Mut<'msg> = ContainedRefsMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct ContainedRefsView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, ContainedRefs>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for ContainedRefsView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for ContainedRefsView<'msg> {
  type Message = ContainedRefs;
}

impl ::std::fmt::Debug for ContainedRefsView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for ContainedRefsView<'_> {
  fn default() -> ContainedRefsView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, ContainedRefs>> for ContainedRefsView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, ContainedRefs>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> ContainedRefsView<'msg> {

  pub fn to_owned(&self) -> ContainedRefs {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // local_name: optional string
  pub fn local_name(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // refs: repeated int64
  pub fn refs(self) -> ::protobuf::RepeatedView<'msg, i64> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<i64>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `ContainedRefsView` is `Sync` because it does not support mutation.
unsafe impl Sync for ContainedRefsView<'_> {}

// SAFETY:
// - `ContainedRefsView` is `Send` because while its alive a `ContainedRefsMut` cannot.
// - `ContainedRefsView` does not use thread-local data.
unsafe impl Send for ContainedRefsView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for ContainedRefsView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for ContainedRefsView<'msg> {}

impl<'msg> ::protobuf::AsView for ContainedRefsView<'msg> {
  type Proxied = ContainedRefs;
  fn as_view(&self) -> ::protobuf::View<'msg, ContainedRefs> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ContainedRefsView<'msg> {
  fn into_view<'shorter>(self) -> ContainedRefsView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<ContainedRefs> for ContainedRefsView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> ContainedRefs {
    let mut dst = ContainedRefs::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<ContainedRefs> for ContainedRefsMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> ContainedRefs {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for ContainedRefs {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for ContainedRefsView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for ContainedRefsMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct ContainedRefsMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, ContainedRefs>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for ContainedRefsMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for ContainedRefsMut<'msg> {
  type Message = ContainedRefs;
}

impl ::std::fmt::Debug for ContainedRefsMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, ContainedRefs>> for ContainedRefsMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, ContainedRefs>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> ContainedRefsMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, ContainedRefs> {
    self.inner
  }

  pub fn to_owned(&self) -> ContainedRefs {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // local_name: optional string
  pub fn local_name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_local_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // refs: repeated int64
  pub fn refs(&self) -> ::protobuf::RepeatedView<'_, i64> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<i64>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn refs_mut(&mut self) -> ::protobuf::RepeatedMut<'_, i64> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_refs(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<i64>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

}

// SAFETY:
// - `ContainedRefsMut` does not perform any shared mutation.
unsafe impl Send for ContainedRefsMut<'_> {}

// SAFETY:
// - `ContainedRefsMut` does not perform any shared mutation.
unsafe impl Sync for ContainedRefsMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for ContainedRefsMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for ContainedRefsMut<'msg> {}

impl<'msg> ::protobuf::AsView for ContainedRefsMut<'msg> {
  type Proxied = ContainedRefs;
  fn as_view(&self) -> ::protobuf::View<'_, ContainedRefs> {
    ContainedRefsView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ContainedRefsMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, ContainedRefs>
  where
      'msg: 'shorter {
    ContainedRefsView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for ContainedRefsMut<'msg> {
  type MutProxied = ContainedRefs;
  fn as_mut(&mut self) -> ContainedRefsMut<'msg> {
    ContainedRefsMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for ContainedRefsMut<'msg> {
  fn into_mut<'shorter>(self) -> ContainedRefsMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl ContainedRefs {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, ContainedRefs> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> ContainedRefsView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> ContainedRefsMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // local_name: optional string
  pub fn local_name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_local_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // refs: repeated int64
  pub fn refs(&self) -> ::protobuf::RepeatedView<'_, i64> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<i64>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn refs_mut(&mut self) -> ::protobuf::RepeatedMut<'_, i64> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_refs(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<i64>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

}  // impl ContainedRefs

impl ::std::ops::Drop for ContainedRefs {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for ContainedRefs {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for ContainedRefs {
  type Proxied = Self;
  fn as_view(&self) -> ContainedRefsView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for ContainedRefs {
  type MutProxied = Self;
  fn as_mut(&mut self) -> ContainedRefsMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for ContainedRefs {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__ContainedRefs_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$N1X?");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__ContainedRefs_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__ContainedRefs_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for ContainedRefs {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for ContainedRefs {
  type Msg = ContainedRefs;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ContainedRefs> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ContainedRefs {
  type Msg = ContainedRefs;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ContainedRefs> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for ContainedRefsMut<'_> {
  type Msg = ContainedRefs;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ContainedRefs> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ContainedRefsMut<'_> {
  type Msg = ContainedRefs;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ContainedRefs> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ContainedRefsView<'_> {
  type Msg = ContainedRefs;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ContainedRefs> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for ContainedRefsMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__StringList_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct StringList {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<StringList>
}

impl ::protobuf::Message for StringList {}

impl ::std::default::Default for StringList {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for StringList {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `StringList` is `Sync` because it does not implement interior mutability.
//    Neither does `StringListMut`.
unsafe impl Sync for StringList {}

// SAFETY:
// - `StringList` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for StringList {}

impl ::protobuf::Proxied for StringList {
  type View<'msg> = StringListView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for StringList {}

impl ::protobuf::MutProxied for StringList {
  type Mut<'msg> = StringListMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct StringListView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, StringList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for StringListView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for StringListView<'msg> {
  type Message = StringList;
}

impl ::std::fmt::Debug for StringListView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for StringListView<'_> {
  fn default() -> StringListView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, StringList>> for StringListView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, StringList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> StringListView<'msg> {

  pub fn to_owned(&self) -> StringList {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // values: repeated string
  pub fn values(self) -> ::protobuf::RepeatedView<'msg, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `StringListView` is `Sync` because it does not support mutation.
unsafe impl Sync for StringListView<'_> {}

// SAFETY:
// - `StringListView` is `Send` because while its alive a `StringListMut` cannot.
// - `StringListView` does not use thread-local data.
unsafe impl Send for StringListView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for StringListView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for StringListView<'msg> {}

impl<'msg> ::protobuf::AsView for StringListView<'msg> {
  type Proxied = StringList;
  fn as_view(&self) -> ::protobuf::View<'msg, StringList> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for StringListView<'msg> {
  fn into_view<'shorter>(self) -> StringListView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<StringList> for StringListView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> StringList {
    let mut dst = StringList::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<StringList> for StringListMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> StringList {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for StringList {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for StringListView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for StringListMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct StringListMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, StringList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for StringListMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for StringListMut<'msg> {
  type Message = StringList;
}

impl ::std::fmt::Debug for StringListMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, StringList>> for StringListMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, StringList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> StringListMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, StringList> {
    self.inner
  }

  pub fn to_owned(&self) -> StringList {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // values: repeated string
  pub fn values(&self) -> ::protobuf::RepeatedView<'_, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn values_mut(&mut self) -> ::protobuf::RepeatedMut<'_, ::protobuf::ProtoString> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_values(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<::protobuf::ProtoString>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}

// SAFETY:
// - `StringListMut` does not perform any shared mutation.
unsafe impl Send for StringListMut<'_> {}

// SAFETY:
// - `StringListMut` does not perform any shared mutation.
unsafe impl Sync for StringListMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for StringListMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for StringListMut<'msg> {}

impl<'msg> ::protobuf::AsView for StringListMut<'msg> {
  type Proxied = StringList;
  fn as_view(&self) -> ::protobuf::View<'_, StringList> {
    StringListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for StringListMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, StringList>
  where
      'msg: 'shorter {
    StringListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for StringListMut<'msg> {
  type MutProxied = StringList;
  fn as_mut(&mut self) -> StringListMut<'msg> {
    StringListMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for StringListMut<'msg> {
  fn into_mut<'shorter>(self) -> StringListMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl StringList {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, StringList> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> StringListView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> StringListMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // values: repeated string
  pub fn values(&self) -> ::protobuf::RepeatedView<'_, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn values_mut(&mut self) -> ::protobuf::RepeatedMut<'_, ::protobuf::ProtoString> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_values(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<::protobuf::ProtoString>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}  // impl StringList

impl ::std::ops::Drop for StringList {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for StringList {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for StringList {
  type Proxied = Self;
  fn as_view(&self) -> StringListView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for StringList {
  type MutProxied = Self;
  fn as_mut(&mut self) -> StringListMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for StringList {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__StringList_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$ME");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__StringList_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__StringList_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for StringList {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for StringList {
  type Msg = StringList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<StringList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for StringList {
  type Msg = StringList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<StringList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for StringListMut<'_> {
  type Msg = StringList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<StringList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for StringListMut<'_> {
  type Msg = StringList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<StringList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for StringListView<'_> {
  type Msg = StringList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<StringList> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for StringListMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__BoolList_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct BoolList {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<BoolList>
}

impl ::protobuf::Message for BoolList {}

impl ::std::default::Default for BoolList {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for BoolList {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `BoolList` is `Sync` because it does not implement interior mutability.
//    Neither does `BoolListMut`.
unsafe impl Sync for BoolList {}

// SAFETY:
// - `BoolList` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for BoolList {}

impl ::protobuf::Proxied for BoolList {
  type View<'msg> = BoolListView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for BoolList {}

impl ::protobuf::MutProxied for BoolList {
  type Mut<'msg> = BoolListMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct BoolListView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, BoolList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for BoolListView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for BoolListView<'msg> {
  type Message = BoolList;
}

impl ::std::fmt::Debug for BoolListView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for BoolListView<'_> {
  fn default() -> BoolListView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, BoolList>> for BoolListView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, BoolList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> BoolListView<'msg> {

  pub fn to_owned(&self) -> BoolList {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // values: repeated bool
  pub fn values(self) -> ::protobuf::RepeatedView<'msg, bool> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<bool>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `BoolListView` is `Sync` because it does not support mutation.
unsafe impl Sync for BoolListView<'_> {}

// SAFETY:
// - `BoolListView` is `Send` because while its alive a `BoolListMut` cannot.
// - `BoolListView` does not use thread-local data.
unsafe impl Send for BoolListView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for BoolListView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for BoolListView<'msg> {}

impl<'msg> ::protobuf::AsView for BoolListView<'msg> {
  type Proxied = BoolList;
  fn as_view(&self) -> ::protobuf::View<'msg, BoolList> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for BoolListView<'msg> {
  fn into_view<'shorter>(self) -> BoolListView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<BoolList> for BoolListView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> BoolList {
    let mut dst = BoolList::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<BoolList> for BoolListMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> BoolList {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for BoolList {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for BoolListView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for BoolListMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct BoolListMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, BoolList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for BoolListMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for BoolListMut<'msg> {
  type Message = BoolList;
}

impl ::std::fmt::Debug for BoolListMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, BoolList>> for BoolListMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, BoolList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> BoolListMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, BoolList> {
    self.inner
  }

  pub fn to_owned(&self) -> BoolList {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // values: repeated bool
  pub fn values(&self) -> ::protobuf::RepeatedView<'_, bool> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<bool>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn values_mut(&mut self) -> ::protobuf::RepeatedMut<'_, bool> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_values(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<bool>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}

// SAFETY:
// - `BoolListMut` does not perform any shared mutation.
unsafe impl Send for BoolListMut<'_> {}

// SAFETY:
// - `BoolListMut` does not perform any shared mutation.
unsafe impl Sync for BoolListMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for BoolListMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for BoolListMut<'msg> {}

impl<'msg> ::protobuf::AsView for BoolListMut<'msg> {
  type Proxied = BoolList;
  fn as_view(&self) -> ::protobuf::View<'_, BoolList> {
    BoolListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for BoolListMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, BoolList>
  where
      'msg: 'shorter {
    BoolListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for BoolListMut<'msg> {
  type MutProxied = BoolList;
  fn as_mut(&mut self) -> BoolListMut<'msg> {
    BoolListMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for BoolListMut<'msg> {
  fn into_mut<'shorter>(self) -> BoolListMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl BoolList {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, BoolList> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> BoolListView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> BoolListMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // values: repeated bool
  pub fn values(&self) -> ::protobuf::RepeatedView<'_, bool> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<bool>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn values_mut(&mut self) -> ::protobuf::RepeatedMut<'_, bool> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_values(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<bool>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}  // impl BoolList

impl ::std::ops::Drop for BoolList {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for BoolList {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for BoolList {
  type Proxied = Self;
  fn as_view(&self) -> BoolListView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for BoolList {
  type MutProxied = Self;
  fn as_mut(&mut self) -> BoolListMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for BoolList {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__BoolList_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$NC");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__BoolList_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__BoolList_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for BoolList {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for BoolList {
  type Msg = BoolList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<BoolList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for BoolList {
  type Msg = BoolList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<BoolList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for BoolListMut<'_> {
  type Msg = BoolList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<BoolList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for BoolListMut<'_> {
  type Msg = BoolList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<BoolList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for BoolListView<'_> {
  type Msg = BoolList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<BoolList> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for BoolListMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__IntList_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct IntList {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<IntList>
}

impl ::protobuf::Message for IntList {}

impl ::std::default::Default for IntList {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for IntList {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `IntList` is `Sync` because it does not implement interior mutability.
//    Neither does `IntListMut`.
unsafe impl Sync for IntList {}

// SAFETY:
// - `IntList` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for IntList {}

impl ::protobuf::Proxied for IntList {
  type View<'msg> = IntListView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for IntList {}

impl ::protobuf::MutProxied for IntList {
  type Mut<'msg> = IntListMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct IntListView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, IntList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for IntListView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for IntListView<'msg> {
  type Message = IntList;
}

impl ::std::fmt::Debug for IntListView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for IntListView<'_> {
  fn default() -> IntListView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, IntList>> for IntListView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, IntList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> IntListView<'msg> {

  pub fn to_owned(&self) -> IntList {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // values: repeated int32
  pub fn values(self) -> ::protobuf::RepeatedView<'msg, i32> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<i32>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `IntListView` is `Sync` because it does not support mutation.
unsafe impl Sync for IntListView<'_> {}

// SAFETY:
// - `IntListView` is `Send` because while its alive a `IntListMut` cannot.
// - `IntListView` does not use thread-local data.
unsafe impl Send for IntListView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for IntListView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for IntListView<'msg> {}

impl<'msg> ::protobuf::AsView for IntListView<'msg> {
  type Proxied = IntList;
  fn as_view(&self) -> ::protobuf::View<'msg, IntList> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for IntListView<'msg> {
  fn into_view<'shorter>(self) -> IntListView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<IntList> for IntListView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> IntList {
    let mut dst = IntList::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<IntList> for IntListMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> IntList {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for IntList {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for IntListView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for IntListMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct IntListMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, IntList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for IntListMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for IntListMut<'msg> {
  type Message = IntList;
}

impl ::std::fmt::Debug for IntListMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, IntList>> for IntListMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, IntList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> IntListMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, IntList> {
    self.inner
  }

  pub fn to_owned(&self) -> IntList {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // values: repeated int32
  pub fn values(&self) -> ::protobuf::RepeatedView<'_, i32> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<i32>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn values_mut(&mut self) -> ::protobuf::RepeatedMut<'_, i32> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_values(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<i32>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}

// SAFETY:
// - `IntListMut` does not perform any shared mutation.
unsafe impl Send for IntListMut<'_> {}

// SAFETY:
// - `IntListMut` does not perform any shared mutation.
unsafe impl Sync for IntListMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for IntListMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for IntListMut<'msg> {}

impl<'msg> ::protobuf::AsView for IntListMut<'msg> {
  type Proxied = IntList;
  fn as_view(&self) -> ::protobuf::View<'_, IntList> {
    IntListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for IntListMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, IntList>
  where
      'msg: 'shorter {
    IntListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for IntListMut<'msg> {
  type MutProxied = IntList;
  fn as_mut(&mut self) -> IntListMut<'msg> {
    IntListMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for IntListMut<'msg> {
  fn into_mut<'shorter>(self) -> IntListMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl IntList {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, IntList> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> IntListView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> IntListMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // values: repeated int32
  pub fn values(&self) -> ::protobuf::RepeatedView<'_, i32> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<i32>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn values_mut(&mut self) -> ::protobuf::RepeatedMut<'_, i32> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_values(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<i32>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}  // impl IntList

impl ::std::ops::Drop for IntList {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for IntList {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for IntList {
  type Proxied = Self;
  fn as_view(&self) -> IntListView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for IntList {
  type MutProxied = Self;
  fn as_mut(&mut self) -> IntListMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for IntList {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__IntList_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$N<");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__IntList_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__IntList_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for IntList {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for IntList {
  type Msg = IntList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<IntList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for IntList {
  type Msg = IntList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<IntList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for IntListMut<'_> {
  type Msg = IntList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<IntList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for IntListMut<'_> {
  type Msg = IntList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<IntList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for IntListView<'_> {
  type Msg = IntList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<IntList> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for IntListMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__LongList_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct LongList {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<LongList>
}

impl ::protobuf::Message for LongList {}

impl ::std::default::Default for LongList {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for LongList {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `LongList` is `Sync` because it does not implement interior mutability.
//    Neither does `LongListMut`.
unsafe impl Sync for LongList {}

// SAFETY:
// - `LongList` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for LongList {}

impl ::protobuf::Proxied for LongList {
  type View<'msg> = LongListView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for LongList {}

impl ::protobuf::MutProxied for LongList {
  type Mut<'msg> = LongListMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct LongListView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, LongList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for LongListView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for LongListView<'msg> {
  type Message = LongList;
}

impl ::std::fmt::Debug for LongListView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for LongListView<'_> {
  fn default() -> LongListView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, LongList>> for LongListView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, LongList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> LongListView<'msg> {

  pub fn to_owned(&self) -> LongList {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // values: repeated int64
  pub fn values(self) -> ::protobuf::RepeatedView<'msg, i64> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<i64>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `LongListView` is `Sync` because it does not support mutation.
unsafe impl Sync for LongListView<'_> {}

// SAFETY:
// - `LongListView` is `Send` because while its alive a `LongListMut` cannot.
// - `LongListView` does not use thread-local data.
unsafe impl Send for LongListView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for LongListView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for LongListView<'msg> {}

impl<'msg> ::protobuf::AsView for LongListView<'msg> {
  type Proxied = LongList;
  fn as_view(&self) -> ::protobuf::View<'msg, LongList> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for LongListView<'msg> {
  fn into_view<'shorter>(self) -> LongListView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<LongList> for LongListView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> LongList {
    let mut dst = LongList::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<LongList> for LongListMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> LongList {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for LongList {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for LongListView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for LongListMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct LongListMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, LongList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for LongListMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for LongListMut<'msg> {
  type Message = LongList;
}

impl ::std::fmt::Debug for LongListMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, LongList>> for LongListMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, LongList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> LongListMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, LongList> {
    self.inner
  }

  pub fn to_owned(&self) -> LongList {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // values: repeated int64
  pub fn values(&self) -> ::protobuf::RepeatedView<'_, i64> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<i64>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn values_mut(&mut self) -> ::protobuf::RepeatedMut<'_, i64> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_values(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<i64>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}

// SAFETY:
// - `LongListMut` does not perform any shared mutation.
unsafe impl Send for LongListMut<'_> {}

// SAFETY:
// - `LongListMut` does not perform any shared mutation.
unsafe impl Sync for LongListMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for LongListMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for LongListMut<'msg> {}

impl<'msg> ::protobuf::AsView for LongListMut<'msg> {
  type Proxied = LongList;
  fn as_view(&self) -> ::protobuf::View<'_, LongList> {
    LongListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for LongListMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, LongList>
  where
      'msg: 'shorter {
    LongListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for LongListMut<'msg> {
  type MutProxied = LongList;
  fn as_mut(&mut self) -> LongListMut<'msg> {
    LongListMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for LongListMut<'msg> {
  fn into_mut<'shorter>(self) -> LongListMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl LongList {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, LongList> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> LongListView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> LongListMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // values: repeated int64
  pub fn values(&self) -> ::protobuf::RepeatedView<'_, i64> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<i64>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn values_mut(&mut self) -> ::protobuf::RepeatedMut<'_, i64> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_values(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<i64>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}  // impl LongList

impl ::std::ops::Drop for LongList {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for LongList {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for LongList {
  type Proxied = Self;
  fn as_view(&self) -> LongListView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for LongList {
  type MutProxied = Self;
  fn as_mut(&mut self) -> LongListMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for LongList {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__LongList_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$N?");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__LongList_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__LongList_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for LongList {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for LongList {
  type Msg = LongList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<LongList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for LongList {
  type Msg = LongList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<LongList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for LongListMut<'_> {
  type Msg = LongList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<LongList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for LongListMut<'_> {
  type Msg = LongList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<LongList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for LongListView<'_> {
  type Msg = LongList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<LongList> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for LongListMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__FloatList_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct FloatList {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<FloatList>
}

impl ::protobuf::Message for FloatList {}

impl ::std::default::Default for FloatList {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for FloatList {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `FloatList` is `Sync` because it does not implement interior mutability.
//    Neither does `FloatListMut`.
unsafe impl Sync for FloatList {}

// SAFETY:
// - `FloatList` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for FloatList {}

impl ::protobuf::Proxied for FloatList {
  type View<'msg> = FloatListView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for FloatList {}

impl ::protobuf::MutProxied for FloatList {
  type Mut<'msg> = FloatListMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct FloatListView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, FloatList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for FloatListView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for FloatListView<'msg> {
  type Message = FloatList;
}

impl ::std::fmt::Debug for FloatListView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for FloatListView<'_> {
  fn default() -> FloatListView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, FloatList>> for FloatListView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, FloatList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> FloatListView<'msg> {

  pub fn to_owned(&self) -> FloatList {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // values: repeated float
  pub fn values(self) -> ::protobuf::RepeatedView<'msg, f32> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<f32>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `FloatListView` is `Sync` because it does not support mutation.
unsafe impl Sync for FloatListView<'_> {}

// SAFETY:
// - `FloatListView` is `Send` because while its alive a `FloatListMut` cannot.
// - `FloatListView` does not use thread-local data.
unsafe impl Send for FloatListView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for FloatListView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for FloatListView<'msg> {}

impl<'msg> ::protobuf::AsView for FloatListView<'msg> {
  type Proxied = FloatList;
  fn as_view(&self) -> ::protobuf::View<'msg, FloatList> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for FloatListView<'msg> {
  fn into_view<'shorter>(self) -> FloatListView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<FloatList> for FloatListView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> FloatList {
    let mut dst = FloatList::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<FloatList> for FloatListMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> FloatList {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for FloatList {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for FloatListView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for FloatListMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct FloatListMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, FloatList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for FloatListMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for FloatListMut<'msg> {
  type Message = FloatList;
}

impl ::std::fmt::Debug for FloatListMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, FloatList>> for FloatListMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, FloatList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> FloatListMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, FloatList> {
    self.inner
  }

  pub fn to_owned(&self) -> FloatList {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // values: repeated float
  pub fn values(&self) -> ::protobuf::RepeatedView<'_, f32> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<f32>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn values_mut(&mut self) -> ::protobuf::RepeatedMut<'_, f32> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_values(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<f32>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}

// SAFETY:
// - `FloatListMut` does not perform any shared mutation.
unsafe impl Send for FloatListMut<'_> {}

// SAFETY:
// - `FloatListMut` does not perform any shared mutation.
unsafe impl Sync for FloatListMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for FloatListMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for FloatListMut<'msg> {}

impl<'msg> ::protobuf::AsView for FloatListMut<'msg> {
  type Proxied = FloatList;
  fn as_view(&self) -> ::protobuf::View<'_, FloatList> {
    FloatListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for FloatListMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, FloatList>
  where
      'msg: 'shorter {
    FloatListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for FloatListMut<'msg> {
  type MutProxied = FloatList;
  fn as_mut(&mut self) -> FloatListMut<'msg> {
    FloatListMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for FloatListMut<'msg> {
  fn into_mut<'shorter>(self) -> FloatListMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl FloatList {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, FloatList> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> FloatListView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> FloatListMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // values: repeated float
  pub fn values(&self) -> ::protobuf::RepeatedView<'_, f32> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<f32>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn values_mut(&mut self) -> ::protobuf::RepeatedMut<'_, f32> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_values(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<f32>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}  // impl FloatList

impl ::std::ops::Drop for FloatList {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for FloatList {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for FloatList {
  type Proxied = Self;
  fn as_view(&self) -> FloatListView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for FloatList {
  type MutProxied = Self;
  fn as_mut(&mut self) -> FloatListMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for FloatList {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__FloatList_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$N7");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__FloatList_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__FloatList_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for FloatList {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for FloatList {
  type Msg = FloatList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<FloatList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for FloatList {
  type Msg = FloatList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<FloatList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for FloatListMut<'_> {
  type Msg = FloatList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<FloatList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for FloatListMut<'_> {
  type Msg = FloatList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<FloatList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for FloatListView<'_> {
  type Msg = FloatList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<FloatList> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for FloatListMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__DoubleList_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct DoubleList {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<DoubleList>
}

impl ::protobuf::Message for DoubleList {}

impl ::std::default::Default for DoubleList {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for DoubleList {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `DoubleList` is `Sync` because it does not implement interior mutability.
//    Neither does `DoubleListMut`.
unsafe impl Sync for DoubleList {}

// SAFETY:
// - `DoubleList` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for DoubleList {}

impl ::protobuf::Proxied for DoubleList {
  type View<'msg> = DoubleListView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for DoubleList {}

impl ::protobuf::MutProxied for DoubleList {
  type Mut<'msg> = DoubleListMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct DoubleListView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, DoubleList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for DoubleListView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for DoubleListView<'msg> {
  type Message = DoubleList;
}

impl ::std::fmt::Debug for DoubleListView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for DoubleListView<'_> {
  fn default() -> DoubleListView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, DoubleList>> for DoubleListView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, DoubleList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> DoubleListView<'msg> {

  pub fn to_owned(&self) -> DoubleList {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // values: repeated double
  pub fn values(self) -> ::protobuf::RepeatedView<'msg, f64> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<f64>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `DoubleListView` is `Sync` because it does not support mutation.
unsafe impl Sync for DoubleListView<'_> {}

// SAFETY:
// - `DoubleListView` is `Send` because while its alive a `DoubleListMut` cannot.
// - `DoubleListView` does not use thread-local data.
unsafe impl Send for DoubleListView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for DoubleListView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for DoubleListView<'msg> {}

impl<'msg> ::protobuf::AsView for DoubleListView<'msg> {
  type Proxied = DoubleList;
  fn as_view(&self) -> ::protobuf::View<'msg, DoubleList> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for DoubleListView<'msg> {
  fn into_view<'shorter>(self) -> DoubleListView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<DoubleList> for DoubleListView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> DoubleList {
    let mut dst = DoubleList::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<DoubleList> for DoubleListMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> DoubleList {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for DoubleList {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for DoubleListView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for DoubleListMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct DoubleListMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, DoubleList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for DoubleListMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for DoubleListMut<'msg> {
  type Message = DoubleList;
}

impl ::std::fmt::Debug for DoubleListMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, DoubleList>> for DoubleListMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, DoubleList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> DoubleListMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, DoubleList> {
    self.inner
  }

  pub fn to_owned(&self) -> DoubleList {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // values: repeated double
  pub fn values(&self) -> ::protobuf::RepeatedView<'_, f64> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<f64>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn values_mut(&mut self) -> ::protobuf::RepeatedMut<'_, f64> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_values(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<f64>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}

// SAFETY:
// - `DoubleListMut` does not perform any shared mutation.
unsafe impl Send for DoubleListMut<'_> {}

// SAFETY:
// - `DoubleListMut` does not perform any shared mutation.
unsafe impl Sync for DoubleListMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for DoubleListMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for DoubleListMut<'msg> {}

impl<'msg> ::protobuf::AsView for DoubleListMut<'msg> {
  type Proxied = DoubleList;
  fn as_view(&self) -> ::protobuf::View<'_, DoubleList> {
    DoubleListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for DoubleListMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, DoubleList>
  where
      'msg: 'shorter {
    DoubleListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for DoubleListMut<'msg> {
  type MutProxied = DoubleList;
  fn as_mut(&mut self) -> DoubleListMut<'msg> {
    DoubleListMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for DoubleListMut<'msg> {
  fn into_mut<'shorter>(self) -> DoubleListMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl DoubleList {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, DoubleList> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> DoubleListView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> DoubleListMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // values: repeated double
  pub fn values(&self) -> ::protobuf::RepeatedView<'_, f64> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<f64>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn values_mut(&mut self) -> ::protobuf::RepeatedMut<'_, f64> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_values(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<f64>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}  // impl DoubleList

impl ::std::ops::Drop for DoubleList {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for DoubleList {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for DoubleList {
  type Proxied = Self;
  fn as_view(&self) -> DoubleListView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for DoubleList {
  type MutProxied = Self;
  fn as_mut(&mut self) -> DoubleListMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for DoubleList {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__DoubleList_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$N6");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__DoubleList_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__DoubleList_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for DoubleList {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for DoubleList {
  type Msg = DoubleList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DoubleList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for DoubleList {
  type Msg = DoubleList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DoubleList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for DoubleListMut<'_> {
  type Msg = DoubleList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DoubleList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for DoubleListMut<'_> {
  type Msg = DoubleList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DoubleList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for DoubleListView<'_> {
  type Msg = DoubleList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DoubleList> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for DoubleListMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__CpgStruct_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct CpgStruct {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<CpgStruct>
}

impl ::protobuf::Message for CpgStruct {}

impl ::std::default::Default for CpgStruct {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for CpgStruct {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `CpgStruct` is `Sync` because it does not implement interior mutability.
//    Neither does `CpgStructMut`.
unsafe impl Sync for CpgStruct {}

// SAFETY:
// - `CpgStruct` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for CpgStruct {}

impl ::protobuf::Proxied for CpgStruct {
  type View<'msg> = CpgStructView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for CpgStruct {}

impl ::protobuf::MutProxied for CpgStruct {
  type Mut<'msg> = CpgStructMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct CpgStructView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, CpgStruct>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for CpgStructView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for CpgStructView<'msg> {
  type Message = CpgStruct;
}

impl ::std::fmt::Debug for CpgStructView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for CpgStructView<'_> {
  fn default() -> CpgStructView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, CpgStruct>> for CpgStructView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, CpgStruct>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> CpgStructView<'msg> {

  pub fn to_owned(&self) -> CpgStruct {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // node: repeated message atom.CpgStruct.Node
  pub fn node(self) -> ::protobuf::RepeatedView<'msg, super::cpg_struct::Node> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::cpg_struct::Node>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // edge: repeated message atom.CpgStruct.Edge
  pub fn edge(self) -> ::protobuf::RepeatedView<'msg, super::cpg_struct::Edge> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::cpg_struct::Edge>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `CpgStructView` is `Sync` because it does not support mutation.
unsafe impl Sync for CpgStructView<'_> {}

// SAFETY:
// - `CpgStructView` is `Send` because while its alive a `CpgStructMut` cannot.
// - `CpgStructView` does not use thread-local data.
unsafe impl Send for CpgStructView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for CpgStructView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for CpgStructView<'msg> {}

impl<'msg> ::protobuf::AsView for CpgStructView<'msg> {
  type Proxied = CpgStruct;
  fn as_view(&self) -> ::protobuf::View<'msg, CpgStruct> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for CpgStructView<'msg> {
  fn into_view<'shorter>(self) -> CpgStructView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<CpgStruct> for CpgStructView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> CpgStruct {
    let mut dst = CpgStruct::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<CpgStruct> for CpgStructMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> CpgStruct {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for CpgStruct {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for CpgStructView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for CpgStructMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct CpgStructMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, CpgStruct>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for CpgStructMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for CpgStructMut<'msg> {
  type Message = CpgStruct;
}

impl ::std::fmt::Debug for CpgStructMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, CpgStruct>> for CpgStructMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, CpgStruct>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> CpgStructMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, CpgStruct> {
    self.inner
  }

  pub fn to_owned(&self) -> CpgStruct {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // node: repeated message atom.CpgStruct.Node
  pub fn node(&self) -> ::protobuf::RepeatedView<'_, super::cpg_struct::Node> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::cpg_struct::Node>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn node_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::cpg_struct::Node> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_node(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::cpg_struct::Node>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

  // edge: repeated message atom.CpgStruct.Edge
  pub fn edge(&self) -> ::protobuf::RepeatedView<'_, super::cpg_struct::Edge> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::cpg_struct::Edge>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn edge_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::cpg_struct::Edge> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_edge(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::cpg_struct::Edge>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

}

// SAFETY:
// - `CpgStructMut` does not perform any shared mutation.
unsafe impl Send for CpgStructMut<'_> {}

// SAFETY:
// - `CpgStructMut` does not perform any shared mutation.
unsafe impl Sync for CpgStructMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for CpgStructMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for CpgStructMut<'msg> {}

impl<'msg> ::protobuf::AsView for CpgStructMut<'msg> {
  type Proxied = CpgStruct;
  fn as_view(&self) -> ::protobuf::View<'_, CpgStruct> {
    CpgStructView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for CpgStructMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, CpgStruct>
  where
      'msg: 'shorter {
    CpgStructView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for CpgStructMut<'msg> {
  type MutProxied = CpgStruct;
  fn as_mut(&mut self) -> CpgStructMut<'msg> {
    CpgStructMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for CpgStructMut<'msg> {
  fn into_mut<'shorter>(self) -> CpgStructMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl CpgStruct {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, CpgStruct> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> CpgStructView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> CpgStructMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // node: repeated message atom.CpgStruct.Node
  pub fn node(&self) -> ::protobuf::RepeatedView<'_, super::cpg_struct::Node> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::cpg_struct::Node>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn node_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::cpg_struct::Node> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_node(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::cpg_struct::Node>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

  // edge: repeated message atom.CpgStruct.Edge
  pub fn edge(&self) -> ::protobuf::RepeatedView<'_, super::cpg_struct::Edge> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::cpg_struct::Edge>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn edge_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::cpg_struct::Edge> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_edge(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::cpg_struct::Edge>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

}  // impl CpgStruct

impl ::std::ops::Drop for CpgStruct {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for CpgStruct {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for CpgStruct {
  type Proxied = Self;
  fn as_view(&self) -> CpgStructView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for CpgStruct {
  type MutProxied = Self;
  fn as_mut(&mut self) -> CpgStructMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for CpgStruct {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__CpgStruct_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$GG");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__CpgStruct_msg_init.0, &[<super::cpg_struct::Node as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::cpg_struct::Edge as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__CpgStruct_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for CpgStruct {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for CpgStruct {
  type Msg = CpgStruct;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<CpgStruct> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for CpgStruct {
  type Msg = CpgStruct;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<CpgStruct> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for CpgStructMut<'_> {
  type Msg = CpgStruct;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<CpgStruct> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for CpgStructMut<'_> {
  type Msg = CpgStruct;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<CpgStruct> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for CpgStructView<'_> {
  type Msg = CpgStruct;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<CpgStruct> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for CpgStructMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

pub mod cpg_struct {// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__CpgStruct__Node_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct Node {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<Node>
}

impl ::protobuf::Message for Node {}

impl ::std::default::Default for Node {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for Node {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `Node` is `Sync` because it does not implement interior mutability.
//    Neither does `NodeMut`.
unsafe impl Sync for Node {}

// SAFETY:
// - `Node` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for Node {}

impl ::protobuf::Proxied for Node {
  type View<'msg> = NodeView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for Node {}

impl ::protobuf::MutProxied for Node {
  type Mut<'msg> = NodeMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct NodeView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Node>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for NodeView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for NodeView<'msg> {
  type Message = Node;
}

impl ::std::fmt::Debug for NodeView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for NodeView<'_> {
  fn default() -> NodeView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, Node>> for NodeView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Node>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> NodeView<'msg> {

  pub fn to_owned(&self) -> Node {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // key: optional int64
  pub fn key(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // type: optional enum atom.NodeType
  pub fn r#type(self) -> super::super::NodeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        1, (super::super::NodeType::UnknownNodeType).into()
      ).try_into().unwrap()
    }
  }

  // property: repeated message atom.CpgStruct.Node.Property
  pub fn property(self) -> ::protobuf::RepeatedView<'msg, super::super::cpg_struct::node::Property> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::cpg_struct::node::Property>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `NodeView` is `Sync` because it does not support mutation.
unsafe impl Sync for NodeView<'_> {}

// SAFETY:
// - `NodeView` is `Send` because while its alive a `NodeMut` cannot.
// - `NodeView` does not use thread-local data.
unsafe impl Send for NodeView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for NodeView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for NodeView<'msg> {}

impl<'msg> ::protobuf::AsView for NodeView<'msg> {
  type Proxied = Node;
  fn as_view(&self) -> ::protobuf::View<'msg, Node> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for NodeView<'msg> {
  fn into_view<'shorter>(self) -> NodeView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<Node> for NodeView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Node {
    let mut dst = Node::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<Node> for NodeMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Node {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for Node {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for NodeView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for NodeMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct NodeMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Node>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for NodeMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for NodeMut<'msg> {
  type Message = Node;
}

impl ::std::fmt::Debug for NodeMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, Node>> for NodeMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Node>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> NodeMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, Node> {
    self.inner
  }

  pub fn to_owned(&self) -> Node {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // key: optional int64
  pub fn key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // type: optional enum atom.NodeType
  pub fn r#type(&self) -> super::super::NodeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        1, (super::super::NodeType::UnknownNodeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_type(&mut self, val: super::super::NodeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        1, val.into()
      )
    }
  }

  // property: repeated message atom.CpgStruct.Node.Property
  pub fn property(&self) -> ::protobuf::RepeatedView<'_, super::super::cpg_struct::node::Property> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::cpg_struct::node::Property>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn property_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::cpg_struct::node::Property> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_property(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::cpg_struct::node::Property>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

}

// SAFETY:
// - `NodeMut` does not perform any shared mutation.
unsafe impl Send for NodeMut<'_> {}

// SAFETY:
// - `NodeMut` does not perform any shared mutation.
unsafe impl Sync for NodeMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for NodeMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for NodeMut<'msg> {}

impl<'msg> ::protobuf::AsView for NodeMut<'msg> {
  type Proxied = Node;
  fn as_view(&self) -> ::protobuf::View<'_, Node> {
    NodeView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for NodeMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, Node>
  where
      'msg: 'shorter {
    NodeView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for NodeMut<'msg> {
  type MutProxied = Node;
  fn as_mut(&mut self) -> NodeMut<'msg> {
    NodeMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for NodeMut<'msg> {
  fn into_mut<'shorter>(self) -> NodeMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl Node {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, Node> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> NodeView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> NodeMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // key: optional int64
  pub fn key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // type: optional enum atom.NodeType
  pub fn r#type(&self) -> super::super::NodeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        1, (super::super::NodeType::UnknownNodeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_type(&mut self, val: super::super::NodeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        1, val.into()
      )
    }
  }

  // property: repeated message atom.CpgStruct.Node.Property
  pub fn property(&self) -> ::protobuf::RepeatedView<'_, super::super::cpg_struct::node::Property> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::cpg_struct::node::Property>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn property_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::cpg_struct::node::Property> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_property(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::cpg_struct::node::Property>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

}  // impl Node

impl ::std::ops::Drop for Node {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for Node {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for Node {
  type Proxied = Self;
  fn as_view(&self) -> NodeView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for Node {
  type MutProxied = Self;
  fn as_mut(&mut self) -> NodeMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for Node {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::cpg_struct::atom__CpgStruct__Node_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$+P.PG");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::cpg_struct::atom__CpgStruct__Node_msg_init.0, &[<super::super::cpg_struct::node::Property as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::cpg_struct::atom__CpgStruct__Node_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for Node {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for Node {
  type Msg = Node;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Node> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for Node {
  type Msg = Node;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Node> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for NodeMut<'_> {
  type Msg = Node;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Node> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for NodeMut<'_> {
  type Msg = Node;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Node> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for NodeView<'_> {
  type Msg = Node;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Node> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for NodeMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

pub mod node {// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__CpgStruct__Node__Property_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct Property {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<Property>
}

impl ::protobuf::Message for Property {}

impl ::std::default::Default for Property {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for Property {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `Property` is `Sync` because it does not implement interior mutability.
//    Neither does `PropertyMut`.
unsafe impl Sync for Property {}

// SAFETY:
// - `Property` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for Property {}

impl ::protobuf::Proxied for Property {
  type View<'msg> = PropertyView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for Property {}

impl ::protobuf::MutProxied for Property {
  type Mut<'msg> = PropertyMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct PropertyView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Property>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for PropertyView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for PropertyView<'msg> {
  type Message = Property;
}

impl ::std::fmt::Debug for PropertyView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for PropertyView<'_> {
  fn default() -> PropertyView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, Property>> for PropertyView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Property>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> PropertyView<'msg> {

  pub fn to_owned(&self) -> Property {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // name: optional enum atom.NodePropertyName
  pub fn name(self) -> super::super::super::NodePropertyName {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        0, (super::super::super::NodePropertyName::UnknownNodeProperty).into()
      ).try_into().unwrap()
    }
  }

  // value: optional message atom.PropertyValue
  pub fn has_value(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn value_opt(self) -> ::protobuf::Optional<super::super::super::PropertyValueView<'msg>> {
        ::protobuf::Optional::new(self.value(), self.has_value())
  }
  pub fn value(self) -> super::super::super::PropertyValueView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::super::PropertyValueView::default())
  }

}

// SAFETY:
// - `PropertyView` is `Sync` because it does not support mutation.
unsafe impl Sync for PropertyView<'_> {}

// SAFETY:
// - `PropertyView` is `Send` because while its alive a `PropertyMut` cannot.
// - `PropertyView` does not use thread-local data.
unsafe impl Send for PropertyView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for PropertyView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for PropertyView<'msg> {}

impl<'msg> ::protobuf::AsView for PropertyView<'msg> {
  type Proxied = Property;
  fn as_view(&self) -> ::protobuf::View<'msg, Property> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for PropertyView<'msg> {
  fn into_view<'shorter>(self) -> PropertyView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<Property> for PropertyView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Property {
    let mut dst = Property::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<Property> for PropertyMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Property {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for Property {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for PropertyView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for PropertyMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct PropertyMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Property>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for PropertyMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for PropertyMut<'msg> {
  type Message = Property;
}

impl ::std::fmt::Debug for PropertyMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, Property>> for PropertyMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Property>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> PropertyMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, Property> {
    self.inner
  }

  pub fn to_owned(&self) -> Property {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // name: optional enum atom.NodePropertyName
  pub fn name(&self) -> super::super::super::NodePropertyName {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        0, (super::super::super::NodePropertyName::UnknownNodeProperty).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_name(&mut self, val: super::super::super::NodePropertyName) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        0, val.into()
      )
    }
  }

  // value: optional message atom.PropertyValue
  pub fn has_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn value_opt(&self) -> ::protobuf::Optional<super::super::super::PropertyValueView<'_>> {
        ::protobuf::Optional::new(self.value(), self.has_value())
  }
  pub fn value(&self) -> super::super::super::PropertyValueView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::super::PropertyValueView::default())
  }
  pub fn value_mut(&mut self) -> super::super::super::PropertyValueMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_value(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::super::PropertyValue>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

}

// SAFETY:
// - `PropertyMut` does not perform any shared mutation.
unsafe impl Send for PropertyMut<'_> {}

// SAFETY:
// - `PropertyMut` does not perform any shared mutation.
unsafe impl Sync for PropertyMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for PropertyMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for PropertyMut<'msg> {}

impl<'msg> ::protobuf::AsView for PropertyMut<'msg> {
  type Proxied = Property;
  fn as_view(&self) -> ::protobuf::View<'_, Property> {
    PropertyView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for PropertyMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, Property>
  where
      'msg: 'shorter {
    PropertyView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for PropertyMut<'msg> {
  type MutProxied = Property;
  fn as_mut(&mut self) -> PropertyMut<'msg> {
    PropertyMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for PropertyMut<'msg> {
  fn into_mut<'shorter>(self) -> PropertyMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl Property {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, Property> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> PropertyView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> PropertyMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // name: optional enum atom.NodePropertyName
  pub fn name(&self) -> super::super::super::NodePropertyName {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        0, (super::super::super::NodePropertyName::UnknownNodeProperty).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_name(&mut self, val: super::super::super::NodePropertyName) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        0, val.into()
      )
    }
  }

  // value: optional message atom.PropertyValue
  pub fn has_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn value_opt(&self) -> ::protobuf::Optional<super::super::super::PropertyValueView<'_>> {
        ::protobuf::Optional::new(self.value(), self.has_value())
  }
  pub fn value(&self) -> super::super::super::PropertyValueView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::super::PropertyValueView::default())
  }
  pub fn value_mut(&mut self) -> super::super::super::PropertyValueMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_value(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::super::PropertyValue>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

}  // impl Property

impl ::std::ops::Drop for Property {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for Property {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for Property {
  type Proxied = Self;
  fn as_view(&self) -> PropertyView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for Property {
  type MutProxied = Self;
  fn as_mut(&mut self) -> PropertyMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for Property {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::super::cpg_struct::node::atom__CpgStruct__Node__Property_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$.P3");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::super::cpg_struct::node::atom__CpgStruct__Node__Property_msg_init.0, &[<super::super::super::PropertyValue as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::super::cpg_struct::node::atom__CpgStruct__Node__Property_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for Property {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for Property {
  type Msg = Property;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Property> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for Property {
  type Msg = Property;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Property> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for PropertyMut<'_> {
  type Msg = Property;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Property> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for PropertyMut<'_> {
  type Msg = Property;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Property> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for PropertyView<'_> {
  type Msg = Property;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Property> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for PropertyMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



}  // pub mod node

// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__CpgStruct__Edge_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct Edge {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<Edge>
}

impl ::protobuf::Message for Edge {}

impl ::std::default::Default for Edge {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for Edge {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `Edge` is `Sync` because it does not implement interior mutability.
//    Neither does `EdgeMut`.
unsafe impl Sync for Edge {}

// SAFETY:
// - `Edge` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for Edge {}

impl ::protobuf::Proxied for Edge {
  type View<'msg> = EdgeView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for Edge {}

impl ::protobuf::MutProxied for Edge {
  type Mut<'msg> = EdgeMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct EdgeView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Edge>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for EdgeView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for EdgeView<'msg> {
  type Message = Edge;
}

impl ::std::fmt::Debug for EdgeView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for EdgeView<'_> {
  fn default() -> EdgeView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, Edge>> for EdgeView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Edge>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> EdgeView<'msg> {

  pub fn to_owned(&self) -> Edge {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // src: optional int64
  pub fn src(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // dst: optional int64
  pub fn dst(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        1, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // type: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn r#type(self) -> super::super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (super::super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }

  // property: repeated message atom.CpgStruct.Edge.Property
  pub fn property(self) -> ::protobuf::RepeatedView<'msg, super::super::cpg_struct::edge::Property> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        3
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::cpg_struct::edge::Property>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `EdgeView` is `Sync` because it does not support mutation.
unsafe impl Sync for EdgeView<'_> {}

// SAFETY:
// - `EdgeView` is `Send` because while its alive a `EdgeMut` cannot.
// - `EdgeView` does not use thread-local data.
unsafe impl Send for EdgeView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for EdgeView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for EdgeView<'msg> {}

impl<'msg> ::protobuf::AsView for EdgeView<'msg> {
  type Proxied = Edge;
  fn as_view(&self) -> ::protobuf::View<'msg, Edge> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for EdgeView<'msg> {
  fn into_view<'shorter>(self) -> EdgeView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<Edge> for EdgeView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Edge {
    let mut dst = Edge::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<Edge> for EdgeMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Edge {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for Edge {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for EdgeView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for EdgeMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct EdgeMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Edge>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for EdgeMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for EdgeMut<'msg> {
  type Message = Edge;
}

impl ::std::fmt::Debug for EdgeMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, Edge>> for EdgeMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Edge>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> EdgeMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, Edge> {
    self.inner
  }

  pub fn to_owned(&self) -> Edge {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // src: optional int64
  pub fn src(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_src(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // dst: optional int64
  pub fn dst(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        1, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_dst(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        1, val.into()
      )
    }
  }

  // type: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn r#type(&self) -> super::super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (super::super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_type(&mut self, val: super::super::cpg_struct::edge::EdgeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        2, val.into()
      )
    }
  }

  // property: repeated message atom.CpgStruct.Edge.Property
  pub fn property(&self) -> ::protobuf::RepeatedView<'_, super::super::cpg_struct::edge::Property> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        3
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::cpg_struct::edge::Property>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn property_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::cpg_struct::edge::Property> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        3,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_property(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::cpg_struct::edge::Property>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                3,
                inner.raw());
    }
  }

}

// SAFETY:
// - `EdgeMut` does not perform any shared mutation.
unsafe impl Send for EdgeMut<'_> {}

// SAFETY:
// - `EdgeMut` does not perform any shared mutation.
unsafe impl Sync for EdgeMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for EdgeMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for EdgeMut<'msg> {}

impl<'msg> ::protobuf::AsView for EdgeMut<'msg> {
  type Proxied = Edge;
  fn as_view(&self) -> ::protobuf::View<'_, Edge> {
    EdgeView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for EdgeMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, Edge>
  where
      'msg: 'shorter {
    EdgeView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for EdgeMut<'msg> {
  type MutProxied = Edge;
  fn as_mut(&mut self) -> EdgeMut<'msg> {
    EdgeMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for EdgeMut<'msg> {
  fn into_mut<'shorter>(self) -> EdgeMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl Edge {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, Edge> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> EdgeView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> EdgeMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // src: optional int64
  pub fn src(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_src(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // dst: optional int64
  pub fn dst(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        1, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_dst(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        1, val.into()
      )
    }
  }

  // type: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn r#type(&self) -> super::super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (super::super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_type(&mut self, val: super::super::cpg_struct::edge::EdgeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        2, val.into()
      )
    }
  }

  // property: repeated message atom.CpgStruct.Edge.Property
  pub fn property(&self) -> ::protobuf::RepeatedView<'_, super::super::cpg_struct::edge::Property> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        3
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::cpg_struct::edge::Property>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn property_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::cpg_struct::edge::Property> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        3,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_property(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::cpg_struct::edge::Property>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                3,
                inner.raw());
    }
  }

}  // impl Edge

impl ::std::ops::Drop for Edge {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for Edge {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for Edge {
  type Proxied = Self;
  fn as_view(&self) -> EdgeView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for Edge {
  type MutProxied = Self;
  fn as_mut(&mut self) -> EdgeMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for Edge {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::cpg_struct::atom__CpgStruct__Edge_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$+P+P.PG");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::cpg_struct::atom__CpgStruct__Edge_msg_init.0, &[<super::super::cpg_struct::edge::Property as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::cpg_struct::atom__CpgStruct__Edge_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for Edge {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for Edge {
  type Msg = Edge;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Edge> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for Edge {
  type Msg = Edge;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Edge> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for EdgeMut<'_> {
  type Msg = Edge;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Edge> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for EdgeMut<'_> {
  type Msg = Edge;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Edge> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for EdgeView<'_> {
  type Msg = Edge;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Edge> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for EdgeMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

pub mod edge {// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__CpgStruct__Edge__Property_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct Property {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<Property>
}

impl ::protobuf::Message for Property {}

impl ::std::default::Default for Property {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for Property {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `Property` is `Sync` because it does not implement interior mutability.
//    Neither does `PropertyMut`.
unsafe impl Sync for Property {}

// SAFETY:
// - `Property` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for Property {}

impl ::protobuf::Proxied for Property {
  type View<'msg> = PropertyView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for Property {}

impl ::protobuf::MutProxied for Property {
  type Mut<'msg> = PropertyMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct PropertyView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Property>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for PropertyView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for PropertyView<'msg> {
  type Message = Property;
}

impl ::std::fmt::Debug for PropertyView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for PropertyView<'_> {
  fn default() -> PropertyView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, Property>> for PropertyView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Property>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> PropertyView<'msg> {

  pub fn to_owned(&self) -> Property {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // name: optional enum atom.EdgePropertyName
  pub fn name(self) -> super::super::super::EdgePropertyName {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        0, (super::super::super::EdgePropertyName::UnknownEdgeProperty).into()
      ).try_into().unwrap()
    }
  }

  // value: optional message atom.PropertyValue
  pub fn has_value(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn value_opt(self) -> ::protobuf::Optional<super::super::super::PropertyValueView<'msg>> {
        ::protobuf::Optional::new(self.value(), self.has_value())
  }
  pub fn value(self) -> super::super::super::PropertyValueView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::super::PropertyValueView::default())
  }

}

// SAFETY:
// - `PropertyView` is `Sync` because it does not support mutation.
unsafe impl Sync for PropertyView<'_> {}

// SAFETY:
// - `PropertyView` is `Send` because while its alive a `PropertyMut` cannot.
// - `PropertyView` does not use thread-local data.
unsafe impl Send for PropertyView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for PropertyView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for PropertyView<'msg> {}

impl<'msg> ::protobuf::AsView for PropertyView<'msg> {
  type Proxied = Property;
  fn as_view(&self) -> ::protobuf::View<'msg, Property> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for PropertyView<'msg> {
  fn into_view<'shorter>(self) -> PropertyView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<Property> for PropertyView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Property {
    let mut dst = Property::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<Property> for PropertyMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Property {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for Property {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for PropertyView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for PropertyMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct PropertyMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Property>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for PropertyMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for PropertyMut<'msg> {
  type Message = Property;
}

impl ::std::fmt::Debug for PropertyMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, Property>> for PropertyMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Property>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> PropertyMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, Property> {
    self.inner
  }

  pub fn to_owned(&self) -> Property {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // name: optional enum atom.EdgePropertyName
  pub fn name(&self) -> super::super::super::EdgePropertyName {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        0, (super::super::super::EdgePropertyName::UnknownEdgeProperty).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_name(&mut self, val: super::super::super::EdgePropertyName) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        0, val.into()
      )
    }
  }

  // value: optional message atom.PropertyValue
  pub fn has_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn value_opt(&self) -> ::protobuf::Optional<super::super::super::PropertyValueView<'_>> {
        ::protobuf::Optional::new(self.value(), self.has_value())
  }
  pub fn value(&self) -> super::super::super::PropertyValueView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::super::PropertyValueView::default())
  }
  pub fn value_mut(&mut self) -> super::super::super::PropertyValueMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_value(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::super::PropertyValue>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

}

// SAFETY:
// - `PropertyMut` does not perform any shared mutation.
unsafe impl Send for PropertyMut<'_> {}

// SAFETY:
// - `PropertyMut` does not perform any shared mutation.
unsafe impl Sync for PropertyMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for PropertyMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for PropertyMut<'msg> {}

impl<'msg> ::protobuf::AsView for PropertyMut<'msg> {
  type Proxied = Property;
  fn as_view(&self) -> ::protobuf::View<'_, Property> {
    PropertyView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for PropertyMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, Property>
  where
      'msg: 'shorter {
    PropertyView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for PropertyMut<'msg> {
  type MutProxied = Property;
  fn as_mut(&mut self) -> PropertyMut<'msg> {
    PropertyMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for PropertyMut<'msg> {
  fn into_mut<'shorter>(self) -> PropertyMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl Property {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, Property> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> PropertyView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> PropertyMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // name: optional enum atom.EdgePropertyName
  pub fn name(&self) -> super::super::super::EdgePropertyName {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        0, (super::super::super::EdgePropertyName::UnknownEdgeProperty).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_name(&mut self, val: super::super::super::EdgePropertyName) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        0, val.into()
      )
    }
  }

  // value: optional message atom.PropertyValue
  pub fn has_value(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_value(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn value_opt(&self) -> ::protobuf::Optional<super::super::super::PropertyValueView<'_>> {
        ::protobuf::Optional::new(self.value(), self.has_value())
  }
  pub fn value(&self) -> super::super::super::PropertyValueView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::super::PropertyValueView::default())
  }
  pub fn value_mut(&mut self) -> super::super::super::PropertyValueMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_value(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::super::PropertyValue>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

}  // impl Property

impl ::std::ops::Drop for Property {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for Property {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for Property {
  type Proxied = Self;
  fn as_view(&self) -> PropertyView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for Property {
  type MutProxied = Self;
  fn as_mut(&mut self) -> PropertyMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for Property {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::super::cpg_struct::edge::atom__CpgStruct__Edge__Property_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$.P3");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::super::cpg_struct::edge::atom__CpgStruct__Edge__Property_msg_init.0, &[<super::super::super::PropertyValue as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::super::cpg_struct::edge::atom__CpgStruct__Edge__Property_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for Property {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for Property {
  type Msg = Property;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Property> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for Property {
  type Msg = Property;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Property> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for PropertyMut<'_> {
  type Msg = Property;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Property> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for PropertyMut<'_> {
  type Msg = Property;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Property> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for PropertyView<'_> {
  type Msg = Property;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Property> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for PropertyMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


#[repr(transparent)]
#[derive(Clone, Copy, PartialEq, Eq, Hash, PartialOrd, Ord)]
pub struct EdgeType(i32);

#[allow(non_upper_case_globals)]
impl EdgeType {
  pub const UnknownEdgeType: EdgeType = EdgeType(0);
  pub const Ast: EdgeType = EdgeType(3);
  pub const Call: EdgeType = EdgeType(6);
  pub const Ref: EdgeType = EdgeType(10);
  pub const TaggedBy: EdgeType = EdgeType(11);
  pub const ParameterLink: EdgeType = EdgeType(12);
  pub const Cfg: EdgeType = EdgeType(19);
  pub const EvalType: EdgeType = EdgeType(21);
  pub const BindsTo: EdgeType = EdgeType(22);
  pub const InheritsFrom: EdgeType = EdgeType(23);
  pub const Contains: EdgeType = EdgeType(28);
  pub const Capture: EdgeType = EdgeType(40);
  pub const CapturedBy: EdgeType = EdgeType(41);
  pub const Receiver: EdgeType = EdgeType(55);
  pub const Condition: EdgeType = EdgeType(56);
  pub const ReachingDef: EdgeType = EdgeType(137);
  pub const AliasOf: EdgeType = EdgeType(138);
  pub const Binds: EdgeType = EdgeType(155);
  pub const Argument: EdgeType = EdgeType(156);
  pub const SourceFile: EdgeType = EdgeType(157);
  pub const Dominate: EdgeType = EdgeType(181);
  pub const PostDominate: EdgeType = EdgeType(182);
  pub const Cdg: EdgeType = EdgeType(183);
  pub const Imports: EdgeType = EdgeType(23663);
  pub const IsCallForImport: EdgeType = EdgeType(23664);

  fn constant_name(&self) -> ::std::option::Option<&'static str> {
    #[allow(unreachable_patterns)] // In the case of aliases, just emit them all and let the first one match.
    Some(match self.0 {
      0 => "UnknownEdgeType",
      3 => "Ast",
      6 => "Call",
      10 => "Ref",
      11 => "TaggedBy",
      12 => "ParameterLink",
      19 => "Cfg",
      21 => "EvalType",
      22 => "BindsTo",
      23 => "InheritsFrom",
      28 => "Contains",
      40 => "Capture",
      41 => "CapturedBy",
      55 => "Receiver",
      56 => "Condition",
      137 => "ReachingDef",
      138 => "AliasOf",
      155 => "Binds",
      156 => "Argument",
      157 => "SourceFile",
      181 => "Dominate",
      182 => "PostDominate",
      183 => "Cdg",
      23663 => "Imports",
      23664 => "IsCallForImport",
      _ => return None
    })
  }
}

impl ::std::convert::From<EdgeType> for i32 {
  fn from(val: EdgeType) -> i32 {
    val.0
  }
}

impl ::std::convert::From<i32> for EdgeType {
  fn from(val: i32) -> EdgeType {
    Self(val)
  }
}

impl ::std::default::Default for EdgeType {
  fn default() -> Self {
    Self(0)
  }
}

impl ::std::fmt::Debug for EdgeType {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    if let Some(constant_name) = self.constant_name() {
      write!(f, "EdgeType::{}", constant_name)
    } else {
      write!(f, "EdgeType::from({})", self.0)
    }
  }
}

impl ::protobuf::IntoProxied<i32> for EdgeType {
  fn into_proxied(self, _: ::protobuf::__internal::Private) -> i32 {
    self.0
  }
}

impl ::protobuf::__internal::SealedInternal for EdgeType {}

impl ::protobuf::Proxied for EdgeType {
  type View<'a> = EdgeType;
}

impl ::protobuf::Proxy<'_> for EdgeType {}
impl ::protobuf::ViewProxy<'_> for EdgeType {}

impl ::protobuf::AsView for EdgeType {
  type Proxied = EdgeType;

  fn as_view(&self) -> EdgeType {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for EdgeType {
  fn into_view<'shorter>(self) -> EdgeType where 'msg: 'shorter {
    self
  }
}

// SAFETY: this is an enum type
unsafe impl ::protobuf::__internal::Enum for EdgeType {
  const NAME: &'static str = "EdgeType";

  fn is_known(value: i32) -> bool {
    matches!(value, 0|3|6|10|11|12|19|21|22|23|28|40|41|55|56|137|138|155|156|157|181|182|183|23663|23664)
  }
}

impl ::protobuf::__internal::runtime::EntityType for EdgeType {
    type Tag = ::protobuf::__internal::runtime::EnumTag;
}


}  // pub mod edge


}  // pub mod cpg_struct


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__AdditionalNodeProperty_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct AdditionalNodeProperty {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<AdditionalNodeProperty>
}

impl ::protobuf::Message for AdditionalNodeProperty {}

impl ::std::default::Default for AdditionalNodeProperty {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for AdditionalNodeProperty {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `AdditionalNodeProperty` is `Sync` because it does not implement interior mutability.
//    Neither does `AdditionalNodePropertyMut`.
unsafe impl Sync for AdditionalNodeProperty {}

// SAFETY:
// - `AdditionalNodeProperty` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for AdditionalNodeProperty {}

impl ::protobuf::Proxied for AdditionalNodeProperty {
  type View<'msg> = AdditionalNodePropertyView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for AdditionalNodeProperty {}

impl ::protobuf::MutProxied for AdditionalNodeProperty {
  type Mut<'msg> = AdditionalNodePropertyMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct AdditionalNodePropertyView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, AdditionalNodeProperty>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for AdditionalNodePropertyView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for AdditionalNodePropertyView<'msg> {
  type Message = AdditionalNodeProperty;
}

impl ::std::fmt::Debug for AdditionalNodePropertyView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for AdditionalNodePropertyView<'_> {
  fn default() -> AdditionalNodePropertyView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, AdditionalNodeProperty>> for AdditionalNodePropertyView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, AdditionalNodeProperty>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> AdditionalNodePropertyView<'msg> {

  pub fn to_owned(&self) -> AdditionalNodeProperty {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // node_id: optional int64
  pub fn node_id(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // property: optional message atom.CpgStruct.Node.Property
  pub fn has_property(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn property_opt(self) -> ::protobuf::Optional<super::cpg_struct::node::PropertyView<'msg>> {
        ::protobuf::Optional::new(self.property(), self.has_property())
  }
  pub fn property(self) -> super::cpg_struct::node::PropertyView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::cpg_struct::node::PropertyView::default())
  }

}

// SAFETY:
// - `AdditionalNodePropertyView` is `Sync` because it does not support mutation.
unsafe impl Sync for AdditionalNodePropertyView<'_> {}

// SAFETY:
// - `AdditionalNodePropertyView` is `Send` because while its alive a `AdditionalNodePropertyMut` cannot.
// - `AdditionalNodePropertyView` does not use thread-local data.
unsafe impl Send for AdditionalNodePropertyView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for AdditionalNodePropertyView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for AdditionalNodePropertyView<'msg> {}

impl<'msg> ::protobuf::AsView for AdditionalNodePropertyView<'msg> {
  type Proxied = AdditionalNodeProperty;
  fn as_view(&self) -> ::protobuf::View<'msg, AdditionalNodeProperty> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for AdditionalNodePropertyView<'msg> {
  fn into_view<'shorter>(self) -> AdditionalNodePropertyView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<AdditionalNodeProperty> for AdditionalNodePropertyView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> AdditionalNodeProperty {
    let mut dst = AdditionalNodeProperty::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<AdditionalNodeProperty> for AdditionalNodePropertyMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> AdditionalNodeProperty {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for AdditionalNodeProperty {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for AdditionalNodePropertyView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for AdditionalNodePropertyMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct AdditionalNodePropertyMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, AdditionalNodeProperty>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for AdditionalNodePropertyMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for AdditionalNodePropertyMut<'msg> {
  type Message = AdditionalNodeProperty;
}

impl ::std::fmt::Debug for AdditionalNodePropertyMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, AdditionalNodeProperty>> for AdditionalNodePropertyMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, AdditionalNodeProperty>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> AdditionalNodePropertyMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, AdditionalNodeProperty> {
    self.inner
  }

  pub fn to_owned(&self) -> AdditionalNodeProperty {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // node_id: optional int64
  pub fn node_id(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_node_id(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // property: optional message atom.CpgStruct.Node.Property
  pub fn has_property(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_property(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn property_opt(&self) -> ::protobuf::Optional<super::cpg_struct::node::PropertyView<'_>> {
        ::protobuf::Optional::new(self.property(), self.has_property())
  }
  pub fn property(&self) -> super::cpg_struct::node::PropertyView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::cpg_struct::node::PropertyView::default())
  }
  pub fn property_mut(&mut self) -> super::cpg_struct::node::PropertyMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_property(&mut self,
    val: impl ::protobuf::IntoProxied<super::cpg_struct::node::Property>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

}

// SAFETY:
// - `AdditionalNodePropertyMut` does not perform any shared mutation.
unsafe impl Send for AdditionalNodePropertyMut<'_> {}

// SAFETY:
// - `AdditionalNodePropertyMut` does not perform any shared mutation.
unsafe impl Sync for AdditionalNodePropertyMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for AdditionalNodePropertyMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for AdditionalNodePropertyMut<'msg> {}

impl<'msg> ::protobuf::AsView for AdditionalNodePropertyMut<'msg> {
  type Proxied = AdditionalNodeProperty;
  fn as_view(&self) -> ::protobuf::View<'_, AdditionalNodeProperty> {
    AdditionalNodePropertyView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for AdditionalNodePropertyMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, AdditionalNodeProperty>
  where
      'msg: 'shorter {
    AdditionalNodePropertyView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for AdditionalNodePropertyMut<'msg> {
  type MutProxied = AdditionalNodeProperty;
  fn as_mut(&mut self) -> AdditionalNodePropertyMut<'msg> {
    AdditionalNodePropertyMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for AdditionalNodePropertyMut<'msg> {
  fn into_mut<'shorter>(self) -> AdditionalNodePropertyMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl AdditionalNodeProperty {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, AdditionalNodeProperty> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> AdditionalNodePropertyView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> AdditionalNodePropertyMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // node_id: optional int64
  pub fn node_id(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_node_id(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // property: optional message atom.CpgStruct.Node.Property
  pub fn has_property(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_property(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn property_opt(&self) -> ::protobuf::Optional<super::cpg_struct::node::PropertyView<'_>> {
        ::protobuf::Optional::new(self.property(), self.has_property())
  }
  pub fn property(&self) -> super::cpg_struct::node::PropertyView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::cpg_struct::node::PropertyView::default())
  }
  pub fn property_mut(&mut self) -> super::cpg_struct::node::PropertyMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_property(&mut self,
    val: impl ::protobuf::IntoProxied<super::cpg_struct::node::Property>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

}  // impl AdditionalNodeProperty

impl ::std::ops::Drop for AdditionalNodeProperty {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for AdditionalNodeProperty {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for AdditionalNodeProperty {
  type Proxied = Self;
  fn as_view(&self) -> AdditionalNodePropertyView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for AdditionalNodeProperty {
  type MutProxied = Self;
  fn as_mut(&mut self) -> AdditionalNodePropertyMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for AdditionalNodeProperty {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__AdditionalNodeProperty_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$+P3");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__AdditionalNodeProperty_msg_init.0, &[<super::cpg_struct::node::Property as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__AdditionalNodeProperty_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for AdditionalNodeProperty {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for AdditionalNodeProperty {
  type Msg = AdditionalNodeProperty;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<AdditionalNodeProperty> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for AdditionalNodeProperty {
  type Msg = AdditionalNodeProperty;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<AdditionalNodeProperty> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for AdditionalNodePropertyMut<'_> {
  type Msg = AdditionalNodeProperty;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<AdditionalNodeProperty> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for AdditionalNodePropertyMut<'_> {
  type Msg = AdditionalNodeProperty;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<AdditionalNodeProperty> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for AdditionalNodePropertyView<'_> {
  type Msg = AdditionalNodeProperty;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<AdditionalNodeProperty> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for AdditionalNodePropertyMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__AdditionalEdgeProperty_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct AdditionalEdgeProperty {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<AdditionalEdgeProperty>
}

impl ::protobuf::Message for AdditionalEdgeProperty {}

impl ::std::default::Default for AdditionalEdgeProperty {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for AdditionalEdgeProperty {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `AdditionalEdgeProperty` is `Sync` because it does not implement interior mutability.
//    Neither does `AdditionalEdgePropertyMut`.
unsafe impl Sync for AdditionalEdgeProperty {}

// SAFETY:
// - `AdditionalEdgeProperty` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for AdditionalEdgeProperty {}

impl ::protobuf::Proxied for AdditionalEdgeProperty {
  type View<'msg> = AdditionalEdgePropertyView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for AdditionalEdgeProperty {}

impl ::protobuf::MutProxied for AdditionalEdgeProperty {
  type Mut<'msg> = AdditionalEdgePropertyMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct AdditionalEdgePropertyView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, AdditionalEdgeProperty>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for AdditionalEdgePropertyView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for AdditionalEdgePropertyView<'msg> {
  type Message = AdditionalEdgeProperty;
}

impl ::std::fmt::Debug for AdditionalEdgePropertyView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for AdditionalEdgePropertyView<'_> {
  fn default() -> AdditionalEdgePropertyView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, AdditionalEdgeProperty>> for AdditionalEdgePropertyView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, AdditionalEdgeProperty>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> AdditionalEdgePropertyView<'msg> {

  pub fn to_owned(&self) -> AdditionalEdgeProperty {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // edge_id: optional int64
  pub fn edge_id(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // property: optional message atom.CpgStruct.Edge.Property
  pub fn has_property(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn property_opt(self) -> ::protobuf::Optional<super::cpg_struct::edge::PropertyView<'msg>> {
        ::protobuf::Optional::new(self.property(), self.has_property())
  }
  pub fn property(self) -> super::cpg_struct::edge::PropertyView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::cpg_struct::edge::PropertyView::default())
  }

  // out_node_key: optional int64
  pub fn out_node_key(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        2, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // in_node_key: optional int64
  pub fn in_node_key(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        3, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // edge_type: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn edge_type(self) -> super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        4, (super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }

}

// SAFETY:
// - `AdditionalEdgePropertyView` is `Sync` because it does not support mutation.
unsafe impl Sync for AdditionalEdgePropertyView<'_> {}

// SAFETY:
// - `AdditionalEdgePropertyView` is `Send` because while its alive a `AdditionalEdgePropertyMut` cannot.
// - `AdditionalEdgePropertyView` does not use thread-local data.
unsafe impl Send for AdditionalEdgePropertyView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for AdditionalEdgePropertyView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for AdditionalEdgePropertyView<'msg> {}

impl<'msg> ::protobuf::AsView for AdditionalEdgePropertyView<'msg> {
  type Proxied = AdditionalEdgeProperty;
  fn as_view(&self) -> ::protobuf::View<'msg, AdditionalEdgeProperty> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for AdditionalEdgePropertyView<'msg> {
  fn into_view<'shorter>(self) -> AdditionalEdgePropertyView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<AdditionalEdgeProperty> for AdditionalEdgePropertyView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> AdditionalEdgeProperty {
    let mut dst = AdditionalEdgeProperty::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<AdditionalEdgeProperty> for AdditionalEdgePropertyMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> AdditionalEdgeProperty {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for AdditionalEdgeProperty {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for AdditionalEdgePropertyView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for AdditionalEdgePropertyMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct AdditionalEdgePropertyMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, AdditionalEdgeProperty>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for AdditionalEdgePropertyMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for AdditionalEdgePropertyMut<'msg> {
  type Message = AdditionalEdgeProperty;
}

impl ::std::fmt::Debug for AdditionalEdgePropertyMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, AdditionalEdgeProperty>> for AdditionalEdgePropertyMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, AdditionalEdgeProperty>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> AdditionalEdgePropertyMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, AdditionalEdgeProperty> {
    self.inner
  }

  pub fn to_owned(&self) -> AdditionalEdgeProperty {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // edge_id: optional int64
  pub fn edge_id(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_edge_id(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // property: optional message atom.CpgStruct.Edge.Property
  pub fn has_property(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_property(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn property_opt(&self) -> ::protobuf::Optional<super::cpg_struct::edge::PropertyView<'_>> {
        ::protobuf::Optional::new(self.property(), self.has_property())
  }
  pub fn property(&self) -> super::cpg_struct::edge::PropertyView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::cpg_struct::edge::PropertyView::default())
  }
  pub fn property_mut(&mut self) -> super::cpg_struct::edge::PropertyMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_property(&mut self,
    val: impl ::protobuf::IntoProxied<super::cpg_struct::edge::Property>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

  // out_node_key: optional int64
  pub fn out_node_key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        2, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_out_node_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        2, val.into()
      )
    }
  }

  // in_node_key: optional int64
  pub fn in_node_key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        3, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_in_node_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        3, val.into()
      )
    }
  }

  // edge_type: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn edge_type(&self) -> super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        4, (super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_edge_type(&mut self, val: super::cpg_struct::edge::EdgeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        4, val.into()
      )
    }
  }

}

// SAFETY:
// - `AdditionalEdgePropertyMut` does not perform any shared mutation.
unsafe impl Send for AdditionalEdgePropertyMut<'_> {}

// SAFETY:
// - `AdditionalEdgePropertyMut` does not perform any shared mutation.
unsafe impl Sync for AdditionalEdgePropertyMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for AdditionalEdgePropertyMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for AdditionalEdgePropertyMut<'msg> {}

impl<'msg> ::protobuf::AsView for AdditionalEdgePropertyMut<'msg> {
  type Proxied = AdditionalEdgeProperty;
  fn as_view(&self) -> ::protobuf::View<'_, AdditionalEdgeProperty> {
    AdditionalEdgePropertyView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for AdditionalEdgePropertyMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, AdditionalEdgeProperty>
  where
      'msg: 'shorter {
    AdditionalEdgePropertyView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for AdditionalEdgePropertyMut<'msg> {
  type MutProxied = AdditionalEdgeProperty;
  fn as_mut(&mut self) -> AdditionalEdgePropertyMut<'msg> {
    AdditionalEdgePropertyMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for AdditionalEdgePropertyMut<'msg> {
  fn into_mut<'shorter>(self) -> AdditionalEdgePropertyMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl AdditionalEdgeProperty {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, AdditionalEdgeProperty> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> AdditionalEdgePropertyView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> AdditionalEdgePropertyMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // edge_id: optional int64
  pub fn edge_id(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_edge_id(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // property: optional message atom.CpgStruct.Edge.Property
  pub fn has_property(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_property(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn property_opt(&self) -> ::protobuf::Optional<super::cpg_struct::edge::PropertyView<'_>> {
        ::protobuf::Optional::new(self.property(), self.has_property())
  }
  pub fn property(&self) -> super::cpg_struct::edge::PropertyView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::cpg_struct::edge::PropertyView::default())
  }
  pub fn property_mut(&mut self) -> super::cpg_struct::edge::PropertyMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_property(&mut self,
    val: impl ::protobuf::IntoProxied<super::cpg_struct::edge::Property>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

  // out_node_key: optional int64
  pub fn out_node_key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        2, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_out_node_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        2, val.into()
      )
    }
  }

  // in_node_key: optional int64
  pub fn in_node_key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        3, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_in_node_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        3, val.into()
      )
    }
  }

  // edge_type: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn edge_type(&self) -> super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        4, (super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_edge_type(&mut self, val: super::cpg_struct::edge::EdgeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        4, val.into()
      )
    }
  }

}  // impl AdditionalEdgeProperty

impl ::std::ops::Drop for AdditionalEdgeProperty {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for AdditionalEdgeProperty {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for AdditionalEdgeProperty {
  type Proxied = Self;
  fn as_view(&self) -> AdditionalEdgePropertyView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for AdditionalEdgeProperty {
  type MutProxied = Self;
  fn as_mut(&mut self) -> AdditionalEdgePropertyMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for AdditionalEdgeProperty {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__AdditionalEdgeProperty_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$+P3+P+P.P");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__AdditionalEdgeProperty_msg_init.0, &[<super::cpg_struct::edge::Property as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__AdditionalEdgeProperty_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for AdditionalEdgeProperty {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for AdditionalEdgeProperty {
  type Msg = AdditionalEdgeProperty;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<AdditionalEdgeProperty> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for AdditionalEdgeProperty {
  type Msg = AdditionalEdgeProperty;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<AdditionalEdgeProperty> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for AdditionalEdgePropertyMut<'_> {
  type Msg = AdditionalEdgeProperty;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<AdditionalEdgeProperty> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for AdditionalEdgePropertyMut<'_> {
  type Msg = AdditionalEdgeProperty;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<AdditionalEdgeProperty> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for AdditionalEdgePropertyView<'_> {
  type Msg = AdditionalEdgeProperty;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<AdditionalEdgeProperty> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for AdditionalEdgePropertyMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__CpgOverlay_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct CpgOverlay {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<CpgOverlay>
}

impl ::protobuf::Message for CpgOverlay {}

impl ::std::default::Default for CpgOverlay {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for CpgOverlay {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `CpgOverlay` is `Sync` because it does not implement interior mutability.
//    Neither does `CpgOverlayMut`.
unsafe impl Sync for CpgOverlay {}

// SAFETY:
// - `CpgOverlay` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for CpgOverlay {}

impl ::protobuf::Proxied for CpgOverlay {
  type View<'msg> = CpgOverlayView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for CpgOverlay {}

impl ::protobuf::MutProxied for CpgOverlay {
  type Mut<'msg> = CpgOverlayMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct CpgOverlayView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, CpgOverlay>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for CpgOverlayView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for CpgOverlayView<'msg> {
  type Message = CpgOverlay;
}

impl ::std::fmt::Debug for CpgOverlayView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for CpgOverlayView<'_> {
  fn default() -> CpgOverlayView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, CpgOverlay>> for CpgOverlayView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, CpgOverlay>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> CpgOverlayView<'msg> {

  pub fn to_owned(&self) -> CpgOverlay {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // node: repeated message atom.CpgStruct.Node
  pub fn node(self) -> ::protobuf::RepeatedView<'msg, super::cpg_struct::Node> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::cpg_struct::Node>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // edge: repeated message atom.CpgStruct.Edge
  pub fn edge(self) -> ::protobuf::RepeatedView<'msg, super::cpg_struct::Edge> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::cpg_struct::Edge>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // node_property: repeated message atom.AdditionalNodeProperty
  pub fn node_property(self) -> ::protobuf::RepeatedView<'msg, super::AdditionalNodeProperty> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::AdditionalNodeProperty>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // edge_property: repeated message atom.AdditionalEdgeProperty
  pub fn edge_property(self) -> ::protobuf::RepeatedView<'msg, super::AdditionalEdgeProperty> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        3
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::AdditionalEdgeProperty>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `CpgOverlayView` is `Sync` because it does not support mutation.
unsafe impl Sync for CpgOverlayView<'_> {}

// SAFETY:
// - `CpgOverlayView` is `Send` because while its alive a `CpgOverlayMut` cannot.
// - `CpgOverlayView` does not use thread-local data.
unsafe impl Send for CpgOverlayView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for CpgOverlayView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for CpgOverlayView<'msg> {}

impl<'msg> ::protobuf::AsView for CpgOverlayView<'msg> {
  type Proxied = CpgOverlay;
  fn as_view(&self) -> ::protobuf::View<'msg, CpgOverlay> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for CpgOverlayView<'msg> {
  fn into_view<'shorter>(self) -> CpgOverlayView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<CpgOverlay> for CpgOverlayView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> CpgOverlay {
    let mut dst = CpgOverlay::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<CpgOverlay> for CpgOverlayMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> CpgOverlay {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for CpgOverlay {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for CpgOverlayView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for CpgOverlayMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct CpgOverlayMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, CpgOverlay>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for CpgOverlayMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for CpgOverlayMut<'msg> {
  type Message = CpgOverlay;
}

impl ::std::fmt::Debug for CpgOverlayMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, CpgOverlay>> for CpgOverlayMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, CpgOverlay>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> CpgOverlayMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, CpgOverlay> {
    self.inner
  }

  pub fn to_owned(&self) -> CpgOverlay {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // node: repeated message atom.CpgStruct.Node
  pub fn node(&self) -> ::protobuf::RepeatedView<'_, super::cpg_struct::Node> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::cpg_struct::Node>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn node_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::cpg_struct::Node> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_node(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::cpg_struct::Node>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

  // edge: repeated message atom.CpgStruct.Edge
  pub fn edge(&self) -> ::protobuf::RepeatedView<'_, super::cpg_struct::Edge> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::cpg_struct::Edge>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn edge_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::cpg_struct::Edge> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_edge(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::cpg_struct::Edge>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

  // node_property: repeated message atom.AdditionalNodeProperty
  pub fn node_property(&self) -> ::protobuf::RepeatedView<'_, super::AdditionalNodeProperty> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::AdditionalNodeProperty>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn node_property_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::AdditionalNodeProperty> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_node_property(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::AdditionalNodeProperty>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

  // edge_property: repeated message atom.AdditionalEdgeProperty
  pub fn edge_property(&self) -> ::protobuf::RepeatedView<'_, super::AdditionalEdgeProperty> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        3
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::AdditionalEdgeProperty>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn edge_property_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::AdditionalEdgeProperty> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        3,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_edge_property(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::AdditionalEdgeProperty>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                3,
                inner.raw());
    }
  }

}

// SAFETY:
// - `CpgOverlayMut` does not perform any shared mutation.
unsafe impl Send for CpgOverlayMut<'_> {}

// SAFETY:
// - `CpgOverlayMut` does not perform any shared mutation.
unsafe impl Sync for CpgOverlayMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for CpgOverlayMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for CpgOverlayMut<'msg> {}

impl<'msg> ::protobuf::AsView for CpgOverlayMut<'msg> {
  type Proxied = CpgOverlay;
  fn as_view(&self) -> ::protobuf::View<'_, CpgOverlay> {
    CpgOverlayView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for CpgOverlayMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, CpgOverlay>
  where
      'msg: 'shorter {
    CpgOverlayView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for CpgOverlayMut<'msg> {
  type MutProxied = CpgOverlay;
  fn as_mut(&mut self) -> CpgOverlayMut<'msg> {
    CpgOverlayMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for CpgOverlayMut<'msg> {
  fn into_mut<'shorter>(self) -> CpgOverlayMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl CpgOverlay {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, CpgOverlay> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> CpgOverlayView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> CpgOverlayMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // node: repeated message atom.CpgStruct.Node
  pub fn node(&self) -> ::protobuf::RepeatedView<'_, super::cpg_struct::Node> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::cpg_struct::Node>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn node_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::cpg_struct::Node> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_node(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::cpg_struct::Node>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

  // edge: repeated message atom.CpgStruct.Edge
  pub fn edge(&self) -> ::protobuf::RepeatedView<'_, super::cpg_struct::Edge> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::cpg_struct::Edge>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn edge_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::cpg_struct::Edge> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_edge(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::cpg_struct::Edge>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

  // node_property: repeated message atom.AdditionalNodeProperty
  pub fn node_property(&self) -> ::protobuf::RepeatedView<'_, super::AdditionalNodeProperty> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::AdditionalNodeProperty>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn node_property_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::AdditionalNodeProperty> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_node_property(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::AdditionalNodeProperty>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

  // edge_property: repeated message atom.AdditionalEdgeProperty
  pub fn edge_property(&self) -> ::protobuf::RepeatedView<'_, super::AdditionalEdgeProperty> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        3
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::AdditionalEdgeProperty>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn edge_property_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::AdditionalEdgeProperty> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        3,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_edge_property(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::AdditionalEdgeProperty>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                3,
                inner.raw());
    }
  }

}  // impl CpgOverlay

impl ::std::ops::Drop for CpgOverlay {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for CpgOverlay {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for CpgOverlay {
  type Proxied = Self;
  fn as_view(&self) -> CpgOverlayView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for CpgOverlay {
  type MutProxied = Self;
  fn as_mut(&mut self) -> CpgOverlayMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for CpgOverlay {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__CpgOverlay_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$GGGG");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__CpgOverlay_msg_init.0, &[<super::cpg_struct::Node as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::cpg_struct::Edge as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::AdditionalNodeProperty as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::AdditionalEdgeProperty as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__CpgOverlay_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for CpgOverlay {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for CpgOverlay {
  type Msg = CpgOverlay;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<CpgOverlay> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for CpgOverlay {
  type Msg = CpgOverlay;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<CpgOverlay> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for CpgOverlayMut<'_> {
  type Msg = CpgOverlay;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<CpgOverlay> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for CpgOverlayMut<'_> {
  type Msg = CpgOverlay;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<CpgOverlay> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for CpgOverlayView<'_> {
  type Msg = CpgOverlay;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<CpgOverlay> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for CpgOverlayMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__DiffGraph_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct DiffGraph {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<DiffGraph>
}

impl ::protobuf::Message for DiffGraph {}

impl ::std::default::Default for DiffGraph {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for DiffGraph {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `DiffGraph` is `Sync` because it does not implement interior mutability.
//    Neither does `DiffGraphMut`.
unsafe impl Sync for DiffGraph {}

// SAFETY:
// - `DiffGraph` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for DiffGraph {}

impl ::protobuf::Proxied for DiffGraph {
  type View<'msg> = DiffGraphView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for DiffGraph {}

impl ::protobuf::MutProxied for DiffGraph {
  type Mut<'msg> = DiffGraphMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct DiffGraphView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, DiffGraph>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for DiffGraphView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for DiffGraphView<'msg> {
  type Message = DiffGraph;
}

impl ::std::fmt::Debug for DiffGraphView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for DiffGraphView<'_> {
  fn default() -> DiffGraphView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, DiffGraph>> for DiffGraphView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, DiffGraph>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> DiffGraphView<'msg> {

  pub fn to_owned(&self) -> DiffGraph {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // entries: repeated message atom.DiffGraph.Entry
  pub fn entries(self) -> ::protobuf::RepeatedView<'msg, super::diff_graph::Entry> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::diff_graph::Entry>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `DiffGraphView` is `Sync` because it does not support mutation.
unsafe impl Sync for DiffGraphView<'_> {}

// SAFETY:
// - `DiffGraphView` is `Send` because while its alive a `DiffGraphMut` cannot.
// - `DiffGraphView` does not use thread-local data.
unsafe impl Send for DiffGraphView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for DiffGraphView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for DiffGraphView<'msg> {}

impl<'msg> ::protobuf::AsView for DiffGraphView<'msg> {
  type Proxied = DiffGraph;
  fn as_view(&self) -> ::protobuf::View<'msg, DiffGraph> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for DiffGraphView<'msg> {
  fn into_view<'shorter>(self) -> DiffGraphView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<DiffGraph> for DiffGraphView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> DiffGraph {
    let mut dst = DiffGraph::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<DiffGraph> for DiffGraphMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> DiffGraph {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for DiffGraph {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for DiffGraphView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for DiffGraphMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct DiffGraphMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, DiffGraph>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for DiffGraphMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for DiffGraphMut<'msg> {
  type Message = DiffGraph;
}

impl ::std::fmt::Debug for DiffGraphMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, DiffGraph>> for DiffGraphMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, DiffGraph>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> DiffGraphMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, DiffGraph> {
    self.inner
  }

  pub fn to_owned(&self) -> DiffGraph {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // entries: repeated message atom.DiffGraph.Entry
  pub fn entries(&self) -> ::protobuf::RepeatedView<'_, super::diff_graph::Entry> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::diff_graph::Entry>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn entries_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::diff_graph::Entry> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_entries(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::diff_graph::Entry>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}

// SAFETY:
// - `DiffGraphMut` does not perform any shared mutation.
unsafe impl Send for DiffGraphMut<'_> {}

// SAFETY:
// - `DiffGraphMut` does not perform any shared mutation.
unsafe impl Sync for DiffGraphMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for DiffGraphMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for DiffGraphMut<'msg> {}

impl<'msg> ::protobuf::AsView for DiffGraphMut<'msg> {
  type Proxied = DiffGraph;
  fn as_view(&self) -> ::protobuf::View<'_, DiffGraph> {
    DiffGraphView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for DiffGraphMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, DiffGraph>
  where
      'msg: 'shorter {
    DiffGraphView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for DiffGraphMut<'msg> {
  type MutProxied = DiffGraph;
  fn as_mut(&mut self) -> DiffGraphMut<'msg> {
    DiffGraphMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for DiffGraphMut<'msg> {
  fn into_mut<'shorter>(self) -> DiffGraphMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl DiffGraph {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, DiffGraph> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> DiffGraphView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> DiffGraphMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // entries: repeated message atom.DiffGraph.Entry
  pub fn entries(&self) -> ::protobuf::RepeatedView<'_, super::diff_graph::Entry> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::diff_graph::Entry>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn entries_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::diff_graph::Entry> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_entries(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::diff_graph::Entry>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}  // impl DiffGraph

impl ::std::ops::Drop for DiffGraph {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for DiffGraph {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for DiffGraph {
  type Proxied = Self;
  fn as_view(&self) -> DiffGraphView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for DiffGraph {
  type MutProxied = Self;
  fn as_mut(&mut self) -> DiffGraphMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for DiffGraph {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__DiffGraph_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$G");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__DiffGraph_msg_init.0, &[<super::diff_graph::Entry as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__DiffGraph_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for DiffGraph {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for DiffGraph {
  type Msg = DiffGraph;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DiffGraph> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for DiffGraph {
  type Msg = DiffGraph;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DiffGraph> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for DiffGraphMut<'_> {
  type Msg = DiffGraph;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DiffGraph> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for DiffGraphMut<'_> {
  type Msg = DiffGraph;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DiffGraph> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for DiffGraphView<'_> {
  type Msg = DiffGraph;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DiffGraph> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for DiffGraphMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

pub mod diff_graph {// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__DiffGraph__RemoveNode_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct RemoveNode {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<RemoveNode>
}

impl ::protobuf::Message for RemoveNode {}

impl ::std::default::Default for RemoveNode {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for RemoveNode {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `RemoveNode` is `Sync` because it does not implement interior mutability.
//    Neither does `RemoveNodeMut`.
unsafe impl Sync for RemoveNode {}

// SAFETY:
// - `RemoveNode` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for RemoveNode {}

impl ::protobuf::Proxied for RemoveNode {
  type View<'msg> = RemoveNodeView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for RemoveNode {}

impl ::protobuf::MutProxied for RemoveNode {
  type Mut<'msg> = RemoveNodeMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct RemoveNodeView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, RemoveNode>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for RemoveNodeView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for RemoveNodeView<'msg> {
  type Message = RemoveNode;
}

impl ::std::fmt::Debug for RemoveNodeView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for RemoveNodeView<'_> {
  fn default() -> RemoveNodeView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, RemoveNode>> for RemoveNodeView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, RemoveNode>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> RemoveNodeView<'msg> {

  pub fn to_owned(&self) -> RemoveNode {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // key: optional int64
  pub fn key(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }

}

// SAFETY:
// - `RemoveNodeView` is `Sync` because it does not support mutation.
unsafe impl Sync for RemoveNodeView<'_> {}

// SAFETY:
// - `RemoveNodeView` is `Send` because while its alive a `RemoveNodeMut` cannot.
// - `RemoveNodeView` does not use thread-local data.
unsafe impl Send for RemoveNodeView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for RemoveNodeView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for RemoveNodeView<'msg> {}

impl<'msg> ::protobuf::AsView for RemoveNodeView<'msg> {
  type Proxied = RemoveNode;
  fn as_view(&self) -> ::protobuf::View<'msg, RemoveNode> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for RemoveNodeView<'msg> {
  fn into_view<'shorter>(self) -> RemoveNodeView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<RemoveNode> for RemoveNodeView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> RemoveNode {
    let mut dst = RemoveNode::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<RemoveNode> for RemoveNodeMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> RemoveNode {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for RemoveNode {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for RemoveNodeView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for RemoveNodeMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct RemoveNodeMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveNode>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for RemoveNodeMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for RemoveNodeMut<'msg> {
  type Message = RemoveNode;
}

impl ::std::fmt::Debug for RemoveNodeMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveNode>> for RemoveNodeMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveNode>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> RemoveNodeMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveNode> {
    self.inner
  }

  pub fn to_owned(&self) -> RemoveNode {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // key: optional int64
  pub fn key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

}

// SAFETY:
// - `RemoveNodeMut` does not perform any shared mutation.
unsafe impl Send for RemoveNodeMut<'_> {}

// SAFETY:
// - `RemoveNodeMut` does not perform any shared mutation.
unsafe impl Sync for RemoveNodeMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for RemoveNodeMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for RemoveNodeMut<'msg> {}

impl<'msg> ::protobuf::AsView for RemoveNodeMut<'msg> {
  type Proxied = RemoveNode;
  fn as_view(&self) -> ::protobuf::View<'_, RemoveNode> {
    RemoveNodeView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for RemoveNodeMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, RemoveNode>
  where
      'msg: 'shorter {
    RemoveNodeView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for RemoveNodeMut<'msg> {
  type MutProxied = RemoveNode;
  fn as_mut(&mut self) -> RemoveNodeMut<'msg> {
    RemoveNodeMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for RemoveNodeMut<'msg> {
  fn into_mut<'shorter>(self) -> RemoveNodeMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl RemoveNode {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, RemoveNode> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> RemoveNodeView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> RemoveNodeMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // key: optional int64
  pub fn key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

}  // impl RemoveNode

impl ::std::ops::Drop for RemoveNode {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for RemoveNode {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for RemoveNode {
  type Proxied = Self;
  fn as_view(&self) -> RemoveNodeView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for RemoveNode {
  type MutProxied = Self;
  fn as_mut(&mut self) -> RemoveNodeMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for RemoveNode {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::diff_graph::atom__DiffGraph__RemoveNode_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$+P");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::diff_graph::atom__DiffGraph__RemoveNode_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::diff_graph::atom__DiffGraph__RemoveNode_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for RemoveNode {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for RemoveNode {
  type Msg = RemoveNode;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveNode> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for RemoveNode {
  type Msg = RemoveNode;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveNode> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for RemoveNodeMut<'_> {
  type Msg = RemoveNode;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveNode> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for RemoveNodeMut<'_> {
  type Msg = RemoveNode;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveNode> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for RemoveNodeView<'_> {
  type Msg = RemoveNode;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveNode> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for RemoveNodeMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__DiffGraph__RemoveNodeProperty_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct RemoveNodeProperty {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<RemoveNodeProperty>
}

impl ::protobuf::Message for RemoveNodeProperty {}

impl ::std::default::Default for RemoveNodeProperty {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for RemoveNodeProperty {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `RemoveNodeProperty` is `Sync` because it does not implement interior mutability.
//    Neither does `RemoveNodePropertyMut`.
unsafe impl Sync for RemoveNodeProperty {}

// SAFETY:
// - `RemoveNodeProperty` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for RemoveNodeProperty {}

impl ::protobuf::Proxied for RemoveNodeProperty {
  type View<'msg> = RemoveNodePropertyView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for RemoveNodeProperty {}

impl ::protobuf::MutProxied for RemoveNodeProperty {
  type Mut<'msg> = RemoveNodePropertyMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct RemoveNodePropertyView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, RemoveNodeProperty>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for RemoveNodePropertyView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for RemoveNodePropertyView<'msg> {
  type Message = RemoveNodeProperty;
}

impl ::std::fmt::Debug for RemoveNodePropertyView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for RemoveNodePropertyView<'_> {
  fn default() -> RemoveNodePropertyView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, RemoveNodeProperty>> for RemoveNodePropertyView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, RemoveNodeProperty>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> RemoveNodePropertyView<'msg> {

  pub fn to_owned(&self) -> RemoveNodeProperty {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // key: optional int64
  pub fn key(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // name: optional enum atom.NodePropertyName
  pub fn name(self) -> super::super::NodePropertyName {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        1, (super::super::NodePropertyName::UnknownNodeProperty).into()
      ).try_into().unwrap()
    }
  }

  // local_name: optional string
  pub fn local_name(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        2, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

}

// SAFETY:
// - `RemoveNodePropertyView` is `Sync` because it does not support mutation.
unsafe impl Sync for RemoveNodePropertyView<'_> {}

// SAFETY:
// - `RemoveNodePropertyView` is `Send` because while its alive a `RemoveNodePropertyMut` cannot.
// - `RemoveNodePropertyView` does not use thread-local data.
unsafe impl Send for RemoveNodePropertyView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for RemoveNodePropertyView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for RemoveNodePropertyView<'msg> {}

impl<'msg> ::protobuf::AsView for RemoveNodePropertyView<'msg> {
  type Proxied = RemoveNodeProperty;
  fn as_view(&self) -> ::protobuf::View<'msg, RemoveNodeProperty> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for RemoveNodePropertyView<'msg> {
  fn into_view<'shorter>(self) -> RemoveNodePropertyView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<RemoveNodeProperty> for RemoveNodePropertyView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> RemoveNodeProperty {
    let mut dst = RemoveNodeProperty::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<RemoveNodeProperty> for RemoveNodePropertyMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> RemoveNodeProperty {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for RemoveNodeProperty {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for RemoveNodePropertyView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for RemoveNodePropertyMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct RemoveNodePropertyMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveNodeProperty>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for RemoveNodePropertyMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for RemoveNodePropertyMut<'msg> {
  type Message = RemoveNodeProperty;
}

impl ::std::fmt::Debug for RemoveNodePropertyMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveNodeProperty>> for RemoveNodePropertyMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveNodeProperty>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> RemoveNodePropertyMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveNodeProperty> {
    self.inner
  }

  pub fn to_owned(&self) -> RemoveNodeProperty {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // key: optional int64
  pub fn key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // name: optional enum atom.NodePropertyName
  pub fn name(&self) -> super::super::NodePropertyName {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        1, (super::super::NodePropertyName::UnknownNodeProperty).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_name(&mut self, val: super::super::NodePropertyName) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        1, val.into()
      )
    }
  }

  // local_name: optional string
  pub fn local_name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        2, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_local_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        2,
        view,
      );
    }
  }

}

// SAFETY:
// - `RemoveNodePropertyMut` does not perform any shared mutation.
unsafe impl Send for RemoveNodePropertyMut<'_> {}

// SAFETY:
// - `RemoveNodePropertyMut` does not perform any shared mutation.
unsafe impl Sync for RemoveNodePropertyMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for RemoveNodePropertyMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for RemoveNodePropertyMut<'msg> {}

impl<'msg> ::protobuf::AsView for RemoveNodePropertyMut<'msg> {
  type Proxied = RemoveNodeProperty;
  fn as_view(&self) -> ::protobuf::View<'_, RemoveNodeProperty> {
    RemoveNodePropertyView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for RemoveNodePropertyMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, RemoveNodeProperty>
  where
      'msg: 'shorter {
    RemoveNodePropertyView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for RemoveNodePropertyMut<'msg> {
  type MutProxied = RemoveNodeProperty;
  fn as_mut(&mut self) -> RemoveNodePropertyMut<'msg> {
    RemoveNodePropertyMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for RemoveNodePropertyMut<'msg> {
  fn into_mut<'shorter>(self) -> RemoveNodePropertyMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl RemoveNodeProperty {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, RemoveNodeProperty> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> RemoveNodePropertyView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> RemoveNodePropertyMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // key: optional int64
  pub fn key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // name: optional enum atom.NodePropertyName
  pub fn name(&self) -> super::super::NodePropertyName {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        1, (super::super::NodePropertyName::UnknownNodeProperty).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_name(&mut self, val: super::super::NodePropertyName) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        1, val.into()
      )
    }
  }

  // local_name: optional string
  pub fn local_name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        2, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_local_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        2,
        view,
      );
    }
  }

}  // impl RemoveNodeProperty

impl ::std::ops::Drop for RemoveNodeProperty {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for RemoveNodeProperty {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for RemoveNodeProperty {
  type Proxied = Self;
  fn as_view(&self) -> RemoveNodePropertyView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for RemoveNodeProperty {
  type MutProxied = Self;
  fn as_mut(&mut self) -> RemoveNodePropertyMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for RemoveNodeProperty {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::diff_graph::atom__DiffGraph__RemoveNodeProperty_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$+P.P1X");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::diff_graph::atom__DiffGraph__RemoveNodeProperty_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::diff_graph::atom__DiffGraph__RemoveNodeProperty_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for RemoveNodeProperty {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for RemoveNodeProperty {
  type Msg = RemoveNodeProperty;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveNodeProperty> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for RemoveNodeProperty {
  type Msg = RemoveNodeProperty;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveNodeProperty> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for RemoveNodePropertyMut<'_> {
  type Msg = RemoveNodeProperty;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveNodeProperty> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for RemoveNodePropertyMut<'_> {
  type Msg = RemoveNodeProperty;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveNodeProperty> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for RemoveNodePropertyView<'_> {
  type Msg = RemoveNodeProperty;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveNodeProperty> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for RemoveNodePropertyMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__DiffGraph__RemoveEdge_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct RemoveEdge {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<RemoveEdge>
}

impl ::protobuf::Message for RemoveEdge {}

impl ::std::default::Default for RemoveEdge {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for RemoveEdge {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `RemoveEdge` is `Sync` because it does not implement interior mutability.
//    Neither does `RemoveEdgeMut`.
unsafe impl Sync for RemoveEdge {}

// SAFETY:
// - `RemoveEdge` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for RemoveEdge {}

impl ::protobuf::Proxied for RemoveEdge {
  type View<'msg> = RemoveEdgeView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for RemoveEdge {}

impl ::protobuf::MutProxied for RemoveEdge {
  type Mut<'msg> = RemoveEdgeMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct RemoveEdgeView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, RemoveEdge>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for RemoveEdgeView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for RemoveEdgeView<'msg> {
  type Message = RemoveEdge;
}

impl ::std::fmt::Debug for RemoveEdgeView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for RemoveEdgeView<'_> {
  fn default() -> RemoveEdgeView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, RemoveEdge>> for RemoveEdgeView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, RemoveEdge>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> RemoveEdgeView<'msg> {

  pub fn to_owned(&self) -> RemoveEdge {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // out_node_key: optional int64
  pub fn out_node_key(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // in_node_key: optional int64
  pub fn in_node_key(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        1, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // edge_type: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn edge_type(self) -> super::super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (super::super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }

  // propertiesHash: optional bytes
  pub fn propertiesHash(self) -> ::protobuf::View<'msg, ::protobuf::ProtoBytes> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    unsafe { str_view.as_ref() }
  }

}

// SAFETY:
// - `RemoveEdgeView` is `Sync` because it does not support mutation.
unsafe impl Sync for RemoveEdgeView<'_> {}

// SAFETY:
// - `RemoveEdgeView` is `Send` because while its alive a `RemoveEdgeMut` cannot.
// - `RemoveEdgeView` does not use thread-local data.
unsafe impl Send for RemoveEdgeView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for RemoveEdgeView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for RemoveEdgeView<'msg> {}

impl<'msg> ::protobuf::AsView for RemoveEdgeView<'msg> {
  type Proxied = RemoveEdge;
  fn as_view(&self) -> ::protobuf::View<'msg, RemoveEdge> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for RemoveEdgeView<'msg> {
  fn into_view<'shorter>(self) -> RemoveEdgeView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<RemoveEdge> for RemoveEdgeView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> RemoveEdge {
    let mut dst = RemoveEdge::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<RemoveEdge> for RemoveEdgeMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> RemoveEdge {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for RemoveEdge {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for RemoveEdgeView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for RemoveEdgeMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct RemoveEdgeMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveEdge>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for RemoveEdgeMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for RemoveEdgeMut<'msg> {
  type Message = RemoveEdge;
}

impl ::std::fmt::Debug for RemoveEdgeMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveEdge>> for RemoveEdgeMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveEdge>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> RemoveEdgeMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveEdge> {
    self.inner
  }

  pub fn to_owned(&self) -> RemoveEdge {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // out_node_key: optional int64
  pub fn out_node_key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_out_node_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // in_node_key: optional int64
  pub fn in_node_key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        1, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_in_node_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        1, val.into()
      )
    }
  }

  // edge_type: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn edge_type(&self) -> super::super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (super::super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_edge_type(&mut self, val: super::super::cpg_struct::edge::EdgeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        2, val.into()
      )
    }
  }

  // propertiesHash: optional bytes
  pub fn propertiesHash(&self) -> ::protobuf::View<'_, ::protobuf::ProtoBytes> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    unsafe { str_view.as_ref() }
  }
  pub fn set_propertiesHash(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoBytes>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

}

// SAFETY:
// - `RemoveEdgeMut` does not perform any shared mutation.
unsafe impl Send for RemoveEdgeMut<'_> {}

// SAFETY:
// - `RemoveEdgeMut` does not perform any shared mutation.
unsafe impl Sync for RemoveEdgeMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for RemoveEdgeMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for RemoveEdgeMut<'msg> {}

impl<'msg> ::protobuf::AsView for RemoveEdgeMut<'msg> {
  type Proxied = RemoveEdge;
  fn as_view(&self) -> ::protobuf::View<'_, RemoveEdge> {
    RemoveEdgeView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for RemoveEdgeMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, RemoveEdge>
  where
      'msg: 'shorter {
    RemoveEdgeView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for RemoveEdgeMut<'msg> {
  type MutProxied = RemoveEdge;
  fn as_mut(&mut self) -> RemoveEdgeMut<'msg> {
    RemoveEdgeMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for RemoveEdgeMut<'msg> {
  fn into_mut<'shorter>(self) -> RemoveEdgeMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl RemoveEdge {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, RemoveEdge> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> RemoveEdgeView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> RemoveEdgeMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // out_node_key: optional int64
  pub fn out_node_key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_out_node_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // in_node_key: optional int64
  pub fn in_node_key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        1, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_in_node_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        1, val.into()
      )
    }
  }

  // edge_type: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn edge_type(&self) -> super::super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (super::super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_edge_type(&mut self, val: super::super::cpg_struct::edge::EdgeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        2, val.into()
      )
    }
  }

  // propertiesHash: optional bytes
  pub fn propertiesHash(&self) -> ::protobuf::View<'_, ::protobuf::ProtoBytes> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    unsafe { str_view.as_ref() }
  }
  pub fn set_propertiesHash(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoBytes>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

}  // impl RemoveEdge

impl ::std::ops::Drop for RemoveEdge {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for RemoveEdge {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for RemoveEdge {
  type Proxied = Self;
  fn as_view(&self) -> RemoveEdgeView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for RemoveEdge {
  type MutProxied = Self;
  fn as_mut(&mut self) -> RemoveEdgeMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for RemoveEdge {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::diff_graph::atom__DiffGraph__RemoveEdge_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$+P+P.P0P");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::diff_graph::atom__DiffGraph__RemoveEdge_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::diff_graph::atom__DiffGraph__RemoveEdge_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for RemoveEdge {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for RemoveEdge {
  type Msg = RemoveEdge;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveEdge> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for RemoveEdge {
  type Msg = RemoveEdge;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveEdge> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for RemoveEdgeMut<'_> {
  type Msg = RemoveEdge;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveEdge> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for RemoveEdgeMut<'_> {
  type Msg = RemoveEdge;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveEdge> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for RemoveEdgeView<'_> {
  type Msg = RemoveEdge;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveEdge> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for RemoveEdgeMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__DiffGraph__RemoveEdgeProperty_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct RemoveEdgeProperty {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<RemoveEdgeProperty>
}

impl ::protobuf::Message for RemoveEdgeProperty {}

impl ::std::default::Default for RemoveEdgeProperty {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for RemoveEdgeProperty {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `RemoveEdgeProperty` is `Sync` because it does not implement interior mutability.
//    Neither does `RemoveEdgePropertyMut`.
unsafe impl Sync for RemoveEdgeProperty {}

// SAFETY:
// - `RemoveEdgeProperty` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for RemoveEdgeProperty {}

impl ::protobuf::Proxied for RemoveEdgeProperty {
  type View<'msg> = RemoveEdgePropertyView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for RemoveEdgeProperty {}

impl ::protobuf::MutProxied for RemoveEdgeProperty {
  type Mut<'msg> = RemoveEdgePropertyMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct RemoveEdgePropertyView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, RemoveEdgeProperty>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for RemoveEdgePropertyView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for RemoveEdgePropertyView<'msg> {
  type Message = RemoveEdgeProperty;
}

impl ::std::fmt::Debug for RemoveEdgePropertyView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for RemoveEdgePropertyView<'_> {
  fn default() -> RemoveEdgePropertyView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, RemoveEdgeProperty>> for RemoveEdgePropertyView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, RemoveEdgeProperty>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> RemoveEdgePropertyView<'msg> {

  pub fn to_owned(&self) -> RemoveEdgeProperty {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // out_node_key: optional int64
  pub fn out_node_key(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // in_node_key: optional int64
  pub fn in_node_key(self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        1, (0i64).into()
      ).try_into().unwrap()
    }
  }

  // edge_type: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn edge_type(self) -> super::super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (super::super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }

  // propertiesHash: optional bytes
  pub fn propertiesHash(self) -> ::protobuf::View<'msg, ::protobuf::ProtoBytes> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    unsafe { str_view.as_ref() }
  }

  // property_name: optional enum atom.EdgePropertyName
  pub fn property_name(self) -> super::super::EdgePropertyName {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        4, (super::super::EdgePropertyName::UnknownEdgeProperty).into()
      ).try_into().unwrap()
    }
  }

}

// SAFETY:
// - `RemoveEdgePropertyView` is `Sync` because it does not support mutation.
unsafe impl Sync for RemoveEdgePropertyView<'_> {}

// SAFETY:
// - `RemoveEdgePropertyView` is `Send` because while its alive a `RemoveEdgePropertyMut` cannot.
// - `RemoveEdgePropertyView` does not use thread-local data.
unsafe impl Send for RemoveEdgePropertyView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for RemoveEdgePropertyView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for RemoveEdgePropertyView<'msg> {}

impl<'msg> ::protobuf::AsView for RemoveEdgePropertyView<'msg> {
  type Proxied = RemoveEdgeProperty;
  fn as_view(&self) -> ::protobuf::View<'msg, RemoveEdgeProperty> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for RemoveEdgePropertyView<'msg> {
  fn into_view<'shorter>(self) -> RemoveEdgePropertyView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<RemoveEdgeProperty> for RemoveEdgePropertyView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> RemoveEdgeProperty {
    let mut dst = RemoveEdgeProperty::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<RemoveEdgeProperty> for RemoveEdgePropertyMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> RemoveEdgeProperty {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for RemoveEdgeProperty {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for RemoveEdgePropertyView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for RemoveEdgePropertyMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct RemoveEdgePropertyMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveEdgeProperty>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for RemoveEdgePropertyMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for RemoveEdgePropertyMut<'msg> {
  type Message = RemoveEdgeProperty;
}

impl ::std::fmt::Debug for RemoveEdgePropertyMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveEdgeProperty>> for RemoveEdgePropertyMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveEdgeProperty>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> RemoveEdgePropertyMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, RemoveEdgeProperty> {
    self.inner
  }

  pub fn to_owned(&self) -> RemoveEdgeProperty {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // out_node_key: optional int64
  pub fn out_node_key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_out_node_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // in_node_key: optional int64
  pub fn in_node_key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        1, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_in_node_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        1, val.into()
      )
    }
  }

  // edge_type: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn edge_type(&self) -> super::super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (super::super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_edge_type(&mut self, val: super::super::cpg_struct::edge::EdgeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        2, val.into()
      )
    }
  }

  // propertiesHash: optional bytes
  pub fn propertiesHash(&self) -> ::protobuf::View<'_, ::protobuf::ProtoBytes> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    unsafe { str_view.as_ref() }
  }
  pub fn set_propertiesHash(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoBytes>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

  // property_name: optional enum atom.EdgePropertyName
  pub fn property_name(&self) -> super::super::EdgePropertyName {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        4, (super::super::EdgePropertyName::UnknownEdgeProperty).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_property_name(&mut self, val: super::super::EdgePropertyName) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        4, val.into()
      )
    }
  }

}

// SAFETY:
// - `RemoveEdgePropertyMut` does not perform any shared mutation.
unsafe impl Send for RemoveEdgePropertyMut<'_> {}

// SAFETY:
// - `RemoveEdgePropertyMut` does not perform any shared mutation.
unsafe impl Sync for RemoveEdgePropertyMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for RemoveEdgePropertyMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for RemoveEdgePropertyMut<'msg> {}

impl<'msg> ::protobuf::AsView for RemoveEdgePropertyMut<'msg> {
  type Proxied = RemoveEdgeProperty;
  fn as_view(&self) -> ::protobuf::View<'_, RemoveEdgeProperty> {
    RemoveEdgePropertyView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for RemoveEdgePropertyMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, RemoveEdgeProperty>
  where
      'msg: 'shorter {
    RemoveEdgePropertyView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for RemoveEdgePropertyMut<'msg> {
  type MutProxied = RemoveEdgeProperty;
  fn as_mut(&mut self) -> RemoveEdgePropertyMut<'msg> {
    RemoveEdgePropertyMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for RemoveEdgePropertyMut<'msg> {
  fn into_mut<'shorter>(self) -> RemoveEdgePropertyMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl RemoveEdgeProperty {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, RemoveEdgeProperty> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> RemoveEdgePropertyView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> RemoveEdgePropertyMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // out_node_key: optional int64
  pub fn out_node_key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        0, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_out_node_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        0, val.into()
      )
    }
  }

  // in_node_key: optional int64
  pub fn in_node_key(&self) -> i64 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i64_at_index(
        1, (0i64).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_in_node_key(&mut self, val: i64) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i64_at_index(
        1, val.into()
      )
    }
  }

  // edge_type: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn edge_type(&self) -> super::super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (super::super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_edge_type(&mut self, val: super::super::cpg_struct::edge::EdgeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        2, val.into()
      )
    }
  }

  // propertiesHash: optional bytes
  pub fn propertiesHash(&self) -> ::protobuf::View<'_, ::protobuf::ProtoBytes> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    unsafe { str_view.as_ref() }
  }
  pub fn set_propertiesHash(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoBytes>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

  // property_name: optional enum atom.EdgePropertyName
  pub fn property_name(&self) -> super::super::EdgePropertyName {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        4, (super::super::EdgePropertyName::UnknownEdgeProperty).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_property_name(&mut self, val: super::super::EdgePropertyName) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        4, val.into()
      )
    }
  }

}  // impl RemoveEdgeProperty

impl ::std::ops::Drop for RemoveEdgeProperty {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for RemoveEdgeProperty {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for RemoveEdgeProperty {
  type Proxied = Self;
  fn as_view(&self) -> RemoveEdgePropertyView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for RemoveEdgeProperty {
  type MutProxied = Self;
  fn as_mut(&mut self) -> RemoveEdgePropertyMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for RemoveEdgeProperty {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::diff_graph::atom__DiffGraph__RemoveEdgeProperty_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$+P+P.P0P.P");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::diff_graph::atom__DiffGraph__RemoveEdgeProperty_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::diff_graph::atom__DiffGraph__RemoveEdgeProperty_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for RemoveEdgeProperty {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for RemoveEdgeProperty {
  type Msg = RemoveEdgeProperty;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveEdgeProperty> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for RemoveEdgeProperty {
  type Msg = RemoveEdgeProperty;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveEdgeProperty> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for RemoveEdgePropertyMut<'_> {
  type Msg = RemoveEdgeProperty;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveEdgeProperty> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for RemoveEdgePropertyMut<'_> {
  type Msg = RemoveEdgeProperty;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveEdgeProperty> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for RemoveEdgePropertyView<'_> {
  type Msg = RemoveEdgeProperty;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<RemoveEdgeProperty> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for RemoveEdgePropertyMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__DiffGraph__Entry_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct Entry {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<Entry>
}

impl ::protobuf::Message for Entry {}

impl ::std::default::Default for Entry {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for Entry {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `Entry` is `Sync` because it does not implement interior mutability.
//    Neither does `EntryMut`.
unsafe impl Sync for Entry {}

// SAFETY:
// - `Entry` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for Entry {}

impl ::protobuf::Proxied for Entry {
  type View<'msg> = EntryView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for Entry {}

impl ::protobuf::MutProxied for Entry {
  type Mut<'msg> = EntryMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct EntryView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Entry>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for EntryView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for EntryView<'msg> {
  type Message = Entry;
}

impl ::std::fmt::Debug for EntryView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for EntryView<'_> {
  fn default() -> EntryView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, Entry>> for EntryView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Entry>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> EntryView<'msg> {

  pub fn to_owned(&self) -> Entry {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // node: optional message atom.CpgStruct.Node
  pub fn has_node(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(0)
    }
  }
  pub fn node_opt(self) -> ::protobuf::Optional<super::super::cpg_struct::NodeView<'msg>> {
        ::protobuf::Optional::new(self.node(), self.has_node())
  }
  pub fn node(self) -> super::super::cpg_struct::NodeView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(0)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::cpg_struct::NodeView::default())
  }

  // edge: optional message atom.CpgStruct.Edge
  pub fn has_edge(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn edge_opt(self) -> ::protobuf::Optional<super::super::cpg_struct::EdgeView<'msg>> {
        ::protobuf::Optional::new(self.edge(), self.has_edge())
  }
  pub fn edge(self) -> super::super::cpg_struct::EdgeView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::cpg_struct::EdgeView::default())
  }

  // node_property: optional message atom.AdditionalNodeProperty
  pub fn has_node_property(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(2)
    }
  }
  pub fn node_property_opt(self) -> ::protobuf::Optional<super::super::AdditionalNodePropertyView<'msg>> {
        ::protobuf::Optional::new(self.node_property(), self.has_node_property())
  }
  pub fn node_property(self) -> super::super::AdditionalNodePropertyView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(2)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::AdditionalNodePropertyView::default())
  }

  // edge_property: optional message atom.AdditionalEdgeProperty
  pub fn has_edge_property(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(3)
    }
  }
  pub fn edge_property_opt(self) -> ::protobuf::Optional<super::super::AdditionalEdgePropertyView<'msg>> {
        ::protobuf::Optional::new(self.edge_property(), self.has_edge_property())
  }
  pub fn edge_property(self) -> super::super::AdditionalEdgePropertyView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(3)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::AdditionalEdgePropertyView::default())
  }

  // remove_node: optional message atom.DiffGraph.RemoveNode
  pub fn has_remove_node(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(4)
    }
  }
  pub fn remove_node_opt(self) -> ::protobuf::Optional<super::super::diff_graph::RemoveNodeView<'msg>> {
        ::protobuf::Optional::new(self.remove_node(), self.has_remove_node())
  }
  pub fn remove_node(self) -> super::super::diff_graph::RemoveNodeView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(4)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::diff_graph::RemoveNodeView::default())
  }

  // remove_node_property: optional message atom.DiffGraph.RemoveNodeProperty
  pub fn has_remove_node_property(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(5)
    }
  }
  pub fn remove_node_property_opt(self) -> ::protobuf::Optional<super::super::diff_graph::RemoveNodePropertyView<'msg>> {
        ::protobuf::Optional::new(self.remove_node_property(), self.has_remove_node_property())
  }
  pub fn remove_node_property(self) -> super::super::diff_graph::RemoveNodePropertyView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(5)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::diff_graph::RemoveNodePropertyView::default())
  }

  // remove_edge: optional message atom.DiffGraph.RemoveEdge
  pub fn has_remove_edge(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(6)
    }
  }
  pub fn remove_edge_opt(self) -> ::protobuf::Optional<super::super::diff_graph::RemoveEdgeView<'msg>> {
        ::protobuf::Optional::new(self.remove_edge(), self.has_remove_edge())
  }
  pub fn remove_edge(self) -> super::super::diff_graph::RemoveEdgeView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(6)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::diff_graph::RemoveEdgeView::default())
  }

  // remove_edge_property: optional message atom.DiffGraph.RemoveEdgeProperty
  pub fn has_remove_edge_property(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(7)
    }
  }
  pub fn remove_edge_property_opt(self) -> ::protobuf::Optional<super::super::diff_graph::RemoveEdgePropertyView<'msg>> {
        ::protobuf::Optional::new(self.remove_edge_property(), self.has_remove_edge_property())
  }
  pub fn remove_edge_property(self) -> super::super::diff_graph::RemoveEdgePropertyView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(7)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::diff_graph::RemoveEdgePropertyView::default())
  }

  pub fn value(self) -> super::super::diff_graph::entry::ValueOneof<'msg> {
    match self.value_case() {
      super::super::diff_graph::entry::ValueCase::Node =>
          super::super::diff_graph::entry::ValueOneof::Node(self.node()),
      super::super::diff_graph::entry::ValueCase::Edge =>
          super::super::diff_graph::entry::ValueOneof::Edge(self.edge()),
      super::super::diff_graph::entry::ValueCase::NodeProperty =>
          super::super::diff_graph::entry::ValueOneof::NodeProperty(self.node_property()),
      super::super::diff_graph::entry::ValueCase::EdgeProperty =>
          super::super::diff_graph::entry::ValueOneof::EdgeProperty(self.edge_property()),
      super::super::diff_graph::entry::ValueCase::RemoveNode =>
          super::super::diff_graph::entry::ValueOneof::RemoveNode(self.remove_node()),
      super::super::diff_graph::entry::ValueCase::RemoveNodeProperty =>
          super::super::diff_graph::entry::ValueOneof::RemoveNodeProperty(self.remove_node_property()),
      super::super::diff_graph::entry::ValueCase::RemoveEdge =>
          super::super::diff_graph::entry::ValueOneof::RemoveEdge(self.remove_edge()),
      super::super::diff_graph::entry::ValueCase::RemoveEdgeProperty =>
          super::super::diff_graph::entry::ValueOneof::RemoveEdgeProperty(self.remove_edge_property()),
      _ => super::super::diff_graph::entry::ValueOneof::not_set(std::marker::PhantomData)
    }
  }

  pub fn value_case(self) -> super::super::diff_graph::entry::ValueCase {
    unsafe {
      let field_num = <Self as ::protobuf::__internal::runtime::UpbGetMessagePtr>::get_ptr(
          &self, ::protobuf::__internal::Private)
          .which_oneof_field_number_by_index(0);
      super::super::diff_graph::entry::ValueCase::try_from(field_num).unwrap_unchecked()
    }
  }
}

// SAFETY:
// - `EntryView` is `Sync` because it does not support mutation.
unsafe impl Sync for EntryView<'_> {}

// SAFETY:
// - `EntryView` is `Send` because while its alive a `EntryMut` cannot.
// - `EntryView` does not use thread-local data.
unsafe impl Send for EntryView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for EntryView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for EntryView<'msg> {}

impl<'msg> ::protobuf::AsView for EntryView<'msg> {
  type Proxied = Entry;
  fn as_view(&self) -> ::protobuf::View<'msg, Entry> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for EntryView<'msg> {
  fn into_view<'shorter>(self) -> EntryView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<Entry> for EntryView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Entry {
    let mut dst = Entry::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<Entry> for EntryMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Entry {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for Entry {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for EntryView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for EntryMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct EntryMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Entry>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for EntryMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for EntryMut<'msg> {
  type Message = Entry;
}

impl ::std::fmt::Debug for EntryMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, Entry>> for EntryMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Entry>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> EntryMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, Entry> {
    self.inner
  }

  pub fn to_owned(&self) -> Entry {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // node: optional message atom.CpgStruct.Node
  pub fn has_node(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(0)
    }
  }
  pub fn clear_node(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        0
      );
    }
  }
  pub fn node_opt(&self) -> ::protobuf::Optional<super::super::cpg_struct::NodeView<'_>> {
        ::protobuf::Optional::new(self.node(), self.has_node())
  }
  pub fn node(&self) -> super::super::cpg_struct::NodeView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(0)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::cpg_struct::NodeView::default())
  }
  pub fn node_mut(&mut self) -> super::super::cpg_struct::NodeMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         0, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_node(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::cpg_struct::Node>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        0,
        val
      );
    }
  }

  // edge: optional message atom.CpgStruct.Edge
  pub fn has_edge(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_edge(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn edge_opt(&self) -> ::protobuf::Optional<super::super::cpg_struct::EdgeView<'_>> {
        ::protobuf::Optional::new(self.edge(), self.has_edge())
  }
  pub fn edge(&self) -> super::super::cpg_struct::EdgeView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::cpg_struct::EdgeView::default())
  }
  pub fn edge_mut(&mut self) -> super::super::cpg_struct::EdgeMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_edge(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::cpg_struct::Edge>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

  // node_property: optional message atom.AdditionalNodeProperty
  pub fn has_node_property(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(2)
    }
  }
  pub fn clear_node_property(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        2
      );
    }
  }
  pub fn node_property_opt(&self) -> ::protobuf::Optional<super::super::AdditionalNodePropertyView<'_>> {
        ::protobuf::Optional::new(self.node_property(), self.has_node_property())
  }
  pub fn node_property(&self) -> super::super::AdditionalNodePropertyView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(2)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::AdditionalNodePropertyView::default())
  }
  pub fn node_property_mut(&mut self) -> super::super::AdditionalNodePropertyMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         2, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_node_property(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::AdditionalNodeProperty>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        2,
        val
      );
    }
  }

  // edge_property: optional message atom.AdditionalEdgeProperty
  pub fn has_edge_property(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(3)
    }
  }
  pub fn clear_edge_property(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        3
      );
    }
  }
  pub fn edge_property_opt(&self) -> ::protobuf::Optional<super::super::AdditionalEdgePropertyView<'_>> {
        ::protobuf::Optional::new(self.edge_property(), self.has_edge_property())
  }
  pub fn edge_property(&self) -> super::super::AdditionalEdgePropertyView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(3)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::AdditionalEdgePropertyView::default())
  }
  pub fn edge_property_mut(&mut self) -> super::super::AdditionalEdgePropertyMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         3, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_edge_property(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::AdditionalEdgeProperty>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        3,
        val
      );
    }
  }

  // remove_node: optional message atom.DiffGraph.RemoveNode
  pub fn has_remove_node(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(4)
    }
  }
  pub fn clear_remove_node(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        4
      );
    }
  }
  pub fn remove_node_opt(&self) -> ::protobuf::Optional<super::super::diff_graph::RemoveNodeView<'_>> {
        ::protobuf::Optional::new(self.remove_node(), self.has_remove_node())
  }
  pub fn remove_node(&self) -> super::super::diff_graph::RemoveNodeView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(4)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::diff_graph::RemoveNodeView::default())
  }
  pub fn remove_node_mut(&mut self) -> super::super::diff_graph::RemoveNodeMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         4, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_remove_node(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::diff_graph::RemoveNode>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        4,
        val
      );
    }
  }

  // remove_node_property: optional message atom.DiffGraph.RemoveNodeProperty
  pub fn has_remove_node_property(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(5)
    }
  }
  pub fn clear_remove_node_property(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        5
      );
    }
  }
  pub fn remove_node_property_opt(&self) -> ::protobuf::Optional<super::super::diff_graph::RemoveNodePropertyView<'_>> {
        ::protobuf::Optional::new(self.remove_node_property(), self.has_remove_node_property())
  }
  pub fn remove_node_property(&self) -> super::super::diff_graph::RemoveNodePropertyView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(5)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::diff_graph::RemoveNodePropertyView::default())
  }
  pub fn remove_node_property_mut(&mut self) -> super::super::diff_graph::RemoveNodePropertyMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         5, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_remove_node_property(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::diff_graph::RemoveNodeProperty>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        5,
        val
      );
    }
  }

  // remove_edge: optional message atom.DiffGraph.RemoveEdge
  pub fn has_remove_edge(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(6)
    }
  }
  pub fn clear_remove_edge(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        6
      );
    }
  }
  pub fn remove_edge_opt(&self) -> ::protobuf::Optional<super::super::diff_graph::RemoveEdgeView<'_>> {
        ::protobuf::Optional::new(self.remove_edge(), self.has_remove_edge())
  }
  pub fn remove_edge(&self) -> super::super::diff_graph::RemoveEdgeView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(6)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::diff_graph::RemoveEdgeView::default())
  }
  pub fn remove_edge_mut(&mut self) -> super::super::diff_graph::RemoveEdgeMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         6, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_remove_edge(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::diff_graph::RemoveEdge>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        6,
        val
      );
    }
  }

  // remove_edge_property: optional message atom.DiffGraph.RemoveEdgeProperty
  pub fn has_remove_edge_property(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(7)
    }
  }
  pub fn clear_remove_edge_property(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        7
      );
    }
  }
  pub fn remove_edge_property_opt(&self) -> ::protobuf::Optional<super::super::diff_graph::RemoveEdgePropertyView<'_>> {
        ::protobuf::Optional::new(self.remove_edge_property(), self.has_remove_edge_property())
  }
  pub fn remove_edge_property(&self) -> super::super::diff_graph::RemoveEdgePropertyView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(7)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::diff_graph::RemoveEdgePropertyView::default())
  }
  pub fn remove_edge_property_mut(&mut self) -> super::super::diff_graph::RemoveEdgePropertyMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         7, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_remove_edge_property(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::diff_graph::RemoveEdgeProperty>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        7,
        val
      );
    }
  }

  pub fn value(&self) -> super::super::diff_graph::entry::ValueOneof<'_> {
    match &self.value_case() {
      super::super::diff_graph::entry::ValueCase::Node =>
          super::super::diff_graph::entry::ValueOneof::Node(self.node()),
      super::super::diff_graph::entry::ValueCase::Edge =>
          super::super::diff_graph::entry::ValueOneof::Edge(self.edge()),
      super::super::diff_graph::entry::ValueCase::NodeProperty =>
          super::super::diff_graph::entry::ValueOneof::NodeProperty(self.node_property()),
      super::super::diff_graph::entry::ValueCase::EdgeProperty =>
          super::super::diff_graph::entry::ValueOneof::EdgeProperty(self.edge_property()),
      super::super::diff_graph::entry::ValueCase::RemoveNode =>
          super::super::diff_graph::entry::ValueOneof::RemoveNode(self.remove_node()),
      super::super::diff_graph::entry::ValueCase::RemoveNodeProperty =>
          super::super::diff_graph::entry::ValueOneof::RemoveNodeProperty(self.remove_node_property()),
      super::super::diff_graph::entry::ValueCase::RemoveEdge =>
          super::super::diff_graph::entry::ValueOneof::RemoveEdge(self.remove_edge()),
      super::super::diff_graph::entry::ValueCase::RemoveEdgeProperty =>
          super::super::diff_graph::entry::ValueOneof::RemoveEdgeProperty(self.remove_edge_property()),
      _ => super::super::diff_graph::entry::ValueOneof::not_set(std::marker::PhantomData)
    }
  }

  pub fn value_case(&self) -> super::super::diff_graph::entry::ValueCase {
    unsafe {
      let field_num = <Self as ::protobuf::__internal::runtime::UpbGetMessagePtr>::get_ptr(
          &self, ::protobuf::__internal::Private)
          .which_oneof_field_number_by_index(0);
      super::super::diff_graph::entry::ValueCase::try_from(field_num).unwrap_unchecked()
    }
  }
}

// SAFETY:
// - `EntryMut` does not perform any shared mutation.
unsafe impl Send for EntryMut<'_> {}

// SAFETY:
// - `EntryMut` does not perform any shared mutation.
unsafe impl Sync for EntryMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for EntryMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for EntryMut<'msg> {}

impl<'msg> ::protobuf::AsView for EntryMut<'msg> {
  type Proxied = Entry;
  fn as_view(&self) -> ::protobuf::View<'_, Entry> {
    EntryView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for EntryMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, Entry>
  where
      'msg: 'shorter {
    EntryView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for EntryMut<'msg> {
  type MutProxied = Entry;
  fn as_mut(&mut self) -> EntryMut<'msg> {
    EntryMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for EntryMut<'msg> {
  fn into_mut<'shorter>(self) -> EntryMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl Entry {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, Entry> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> EntryView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> EntryMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // node: optional message atom.CpgStruct.Node
  pub fn has_node(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(0)
    }
  }
  pub fn clear_node(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        0
      );
    }
  }
  pub fn node_opt(&self) -> ::protobuf::Optional<super::super::cpg_struct::NodeView<'_>> {
        ::protobuf::Optional::new(self.node(), self.has_node())
  }
  pub fn node(&self) -> super::super::cpg_struct::NodeView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(0)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::cpg_struct::NodeView::default())
  }
  pub fn node_mut(&mut self) -> super::super::cpg_struct::NodeMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         0, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_node(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::cpg_struct::Node>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        0,
        val
      );
    }
  }

  // edge: optional message atom.CpgStruct.Edge
  pub fn has_edge(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_edge(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn edge_opt(&self) -> ::protobuf::Optional<super::super::cpg_struct::EdgeView<'_>> {
        ::protobuf::Optional::new(self.edge(), self.has_edge())
  }
  pub fn edge(&self) -> super::super::cpg_struct::EdgeView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::cpg_struct::EdgeView::default())
  }
  pub fn edge_mut(&mut self) -> super::super::cpg_struct::EdgeMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_edge(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::cpg_struct::Edge>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

  // node_property: optional message atom.AdditionalNodeProperty
  pub fn has_node_property(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(2)
    }
  }
  pub fn clear_node_property(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        2
      );
    }
  }
  pub fn node_property_opt(&self) -> ::protobuf::Optional<super::super::AdditionalNodePropertyView<'_>> {
        ::protobuf::Optional::new(self.node_property(), self.has_node_property())
  }
  pub fn node_property(&self) -> super::super::AdditionalNodePropertyView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(2)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::AdditionalNodePropertyView::default())
  }
  pub fn node_property_mut(&mut self) -> super::super::AdditionalNodePropertyMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         2, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_node_property(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::AdditionalNodeProperty>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        2,
        val
      );
    }
  }

  // edge_property: optional message atom.AdditionalEdgeProperty
  pub fn has_edge_property(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(3)
    }
  }
  pub fn clear_edge_property(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        3
      );
    }
  }
  pub fn edge_property_opt(&self) -> ::protobuf::Optional<super::super::AdditionalEdgePropertyView<'_>> {
        ::protobuf::Optional::new(self.edge_property(), self.has_edge_property())
  }
  pub fn edge_property(&self) -> super::super::AdditionalEdgePropertyView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(3)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::AdditionalEdgePropertyView::default())
  }
  pub fn edge_property_mut(&mut self) -> super::super::AdditionalEdgePropertyMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         3, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_edge_property(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::AdditionalEdgeProperty>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        3,
        val
      );
    }
  }

  // remove_node: optional message atom.DiffGraph.RemoveNode
  pub fn has_remove_node(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(4)
    }
  }
  pub fn clear_remove_node(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        4
      );
    }
  }
  pub fn remove_node_opt(&self) -> ::protobuf::Optional<super::super::diff_graph::RemoveNodeView<'_>> {
        ::protobuf::Optional::new(self.remove_node(), self.has_remove_node())
  }
  pub fn remove_node(&self) -> super::super::diff_graph::RemoveNodeView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(4)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::diff_graph::RemoveNodeView::default())
  }
  pub fn remove_node_mut(&mut self) -> super::super::diff_graph::RemoveNodeMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         4, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_remove_node(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::diff_graph::RemoveNode>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        4,
        val
      );
    }
  }

  // remove_node_property: optional message atom.DiffGraph.RemoveNodeProperty
  pub fn has_remove_node_property(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(5)
    }
  }
  pub fn clear_remove_node_property(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        5
      );
    }
  }
  pub fn remove_node_property_opt(&self) -> ::protobuf::Optional<super::super::diff_graph::RemoveNodePropertyView<'_>> {
        ::protobuf::Optional::new(self.remove_node_property(), self.has_remove_node_property())
  }
  pub fn remove_node_property(&self) -> super::super::diff_graph::RemoveNodePropertyView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(5)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::diff_graph::RemoveNodePropertyView::default())
  }
  pub fn remove_node_property_mut(&mut self) -> super::super::diff_graph::RemoveNodePropertyMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         5, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_remove_node_property(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::diff_graph::RemoveNodeProperty>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        5,
        val
      );
    }
  }

  // remove_edge: optional message atom.DiffGraph.RemoveEdge
  pub fn has_remove_edge(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(6)
    }
  }
  pub fn clear_remove_edge(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        6
      );
    }
  }
  pub fn remove_edge_opt(&self) -> ::protobuf::Optional<super::super::diff_graph::RemoveEdgeView<'_>> {
        ::protobuf::Optional::new(self.remove_edge(), self.has_remove_edge())
  }
  pub fn remove_edge(&self) -> super::super::diff_graph::RemoveEdgeView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(6)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::diff_graph::RemoveEdgeView::default())
  }
  pub fn remove_edge_mut(&mut self) -> super::super::diff_graph::RemoveEdgeMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         6, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_remove_edge(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::diff_graph::RemoveEdge>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        6,
        val
      );
    }
  }

  // remove_edge_property: optional message atom.DiffGraph.RemoveEdgeProperty
  pub fn has_remove_edge_property(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(7)
    }
  }
  pub fn clear_remove_edge_property(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        7
      );
    }
  }
  pub fn remove_edge_property_opt(&self) -> ::protobuf::Optional<super::super::diff_graph::RemoveEdgePropertyView<'_>> {
        ::protobuf::Optional::new(self.remove_edge_property(), self.has_remove_edge_property())
  }
  pub fn remove_edge_property(&self) -> super::super::diff_graph::RemoveEdgePropertyView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(7)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::diff_graph::RemoveEdgePropertyView::default())
  }
  pub fn remove_edge_property_mut(&mut self) -> super::super::diff_graph::RemoveEdgePropertyMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         7, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_remove_edge_property(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::diff_graph::RemoveEdgeProperty>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        7,
        val
      );
    }
  }

  pub fn value(&self) -> super::super::diff_graph::entry::ValueOneof<'_> {
    match &self.value_case() {
      super::super::diff_graph::entry::ValueCase::Node =>
          super::super::diff_graph::entry::ValueOneof::Node(self.node()),
      super::super::diff_graph::entry::ValueCase::Edge =>
          super::super::diff_graph::entry::ValueOneof::Edge(self.edge()),
      super::super::diff_graph::entry::ValueCase::NodeProperty =>
          super::super::diff_graph::entry::ValueOneof::NodeProperty(self.node_property()),
      super::super::diff_graph::entry::ValueCase::EdgeProperty =>
          super::super::diff_graph::entry::ValueOneof::EdgeProperty(self.edge_property()),
      super::super::diff_graph::entry::ValueCase::RemoveNode =>
          super::super::diff_graph::entry::ValueOneof::RemoveNode(self.remove_node()),
      super::super::diff_graph::entry::ValueCase::RemoveNodeProperty =>
          super::super::diff_graph::entry::ValueOneof::RemoveNodeProperty(self.remove_node_property()),
      super::super::diff_graph::entry::ValueCase::RemoveEdge =>
          super::super::diff_graph::entry::ValueOneof::RemoveEdge(self.remove_edge()),
      super::super::diff_graph::entry::ValueCase::RemoveEdgeProperty =>
          super::super::diff_graph::entry::ValueOneof::RemoveEdgeProperty(self.remove_edge_property()),
      _ => super::super::diff_graph::entry::ValueOneof::not_set(std::marker::PhantomData)
    }
  }

  pub fn value_case(&self) -> super::super::diff_graph::entry::ValueCase {
    unsafe {
      let field_num = <Self as ::protobuf::__internal::runtime::UpbGetMessagePtr>::get_ptr(
          &self, ::protobuf::__internal::Private)
          .which_oneof_field_number_by_index(0);
      super::super::diff_graph::entry::ValueCase::try_from(field_num).unwrap_unchecked()
    }
  }
}  // impl Entry

impl ::std::ops::Drop for Entry {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for Entry {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for Entry {
  type Proxied = Self;
  fn as_view(&self) -> EntryView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for Entry {
  type MutProxied = Self;
  fn as_mut(&mut self) -> EntryMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for Entry {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::diff_graph::atom__DiffGraph__Entry_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$33333333^!|#|$|%|&|(|)|*");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::diff_graph::atom__DiffGraph__Entry_msg_init.0, &[<super::super::cpg_struct::Node as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::super::cpg_struct::Edge as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::super::AdditionalNodeProperty as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::super::AdditionalEdgeProperty as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::super::diff_graph::RemoveNode as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::super::diff_graph::RemoveNodeProperty as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::super::diff_graph::RemoveEdge as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::super::diff_graph::RemoveEdgeProperty as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::diff_graph::atom__DiffGraph__Entry_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for Entry {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for Entry {
  type Msg = Entry;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Entry> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for Entry {
  type Msg = Entry;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Entry> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for EntryMut<'_> {
  type Msg = Entry;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Entry> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for EntryMut<'_> {
  type Msg = Entry;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Entry> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for EntryView<'_> {
  type Msg = Entry;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Entry> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for EntryMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

pub mod entry {

#[non_exhaustive]
#[derive(Debug, Clone, Copy)]
#[allow(dead_code)]
#[repr(u32)]
pub enum ValueOneof<'msg> {
  Node(::protobuf::View<'msg, super::super::super::cpg_struct::Node>) = 1,
  Edge(::protobuf::View<'msg, super::super::super::cpg_struct::Edge>) = 2,
  NodeProperty(::protobuf::View<'msg, super::super::super::AdditionalNodeProperty>) = 3,
  EdgeProperty(::protobuf::View<'msg, super::super::super::AdditionalEdgeProperty>) = 4,
  RemoveNode(::protobuf::View<'msg, super::super::super::diff_graph::RemoveNode>) = 5,
  RemoveNodeProperty(::protobuf::View<'msg, super::super::super::diff_graph::RemoveNodeProperty>) = 6,
  RemoveEdge(::protobuf::View<'msg, super::super::super::diff_graph::RemoveEdge>) = 7,
  RemoveEdgeProperty(::protobuf::View<'msg, super::super::super::diff_graph::RemoveEdgeProperty>) = 8,

  not_set(std::marker::PhantomData<&'msg ()>) = 0
}
#[repr(C)]
#[derive(Debug, Copy, Clone, PartialEq, Eq)]
#[non_exhaustive]
#[allow(dead_code)]
pub enum ValueCase {
  Node = 1,
  Edge = 2,
  NodeProperty = 3,
  EdgeProperty = 4,
  RemoveNode = 5,
  RemoveNodeProperty = 6,
  RemoveEdge = 7,
  RemoveEdgeProperty = 8,

  not_set = 0
}

impl ValueCase {
  #[allow(dead_code)]
  pub(crate) fn try_from(v: u32) -> ::std::option::Option<ValueCase> {
    match v {
      0 => Some(ValueCase::not_set),
      1 => Some(ValueCase::Node),
      2 => Some(ValueCase::Edge),
      3 => Some(ValueCase::NodeProperty),
      4 => Some(ValueCase::EdgeProperty),
      5 => Some(ValueCase::RemoveNode),
      6 => Some(ValueCase::RemoveNodeProperty),
      7 => Some(ValueCase::RemoveEdge),
      8 => Some(ValueCase::RemoveEdgeProperty),
      _ => None
    }
  }
}
}  // pub mod entry


}  // pub mod diff_graph


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__UsageSlice_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct UsageSlice {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<UsageSlice>
}

impl ::protobuf::Message for UsageSlice {}

impl ::std::default::Default for UsageSlice {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for UsageSlice {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `UsageSlice` is `Sync` because it does not implement interior mutability.
//    Neither does `UsageSliceMut`.
unsafe impl Sync for UsageSlice {}

// SAFETY:
// - `UsageSlice` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for UsageSlice {}

impl ::protobuf::Proxied for UsageSlice {
  type View<'msg> = UsageSliceView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for UsageSlice {}

impl ::protobuf::MutProxied for UsageSlice {
  type Mut<'msg> = UsageSliceMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct UsageSliceView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, UsageSlice>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for UsageSliceView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for UsageSliceView<'msg> {
  type Message = UsageSlice;
}

impl ::std::fmt::Debug for UsageSliceView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for UsageSliceView<'_> {
  fn default() -> UsageSliceView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, UsageSlice>> for UsageSliceView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, UsageSlice>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> UsageSliceView<'msg> {

  pub fn to_owned(&self) -> UsageSlice {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // objectSlices: repeated message atom.UsageSlice.MethodUsageSlice
  pub fn objectSlices(self) -> ::protobuf::RepeatedView<'msg, super::usage_slice::MethodUsageSlice> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::usage_slice::MethodUsageSlice>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // userDefinedTypes: repeated message atom.UsageSlice.UserDefinedTypes
  pub fn userDefinedTypes(self) -> ::protobuf::RepeatedView<'msg, super::usage_slice::UserDefinedTypes> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::usage_slice::UserDefinedTypes>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `UsageSliceView` is `Sync` because it does not support mutation.
unsafe impl Sync for UsageSliceView<'_> {}

// SAFETY:
// - `UsageSliceView` is `Send` because while its alive a `UsageSliceMut` cannot.
// - `UsageSliceView` does not use thread-local data.
unsafe impl Send for UsageSliceView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for UsageSliceView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for UsageSliceView<'msg> {}

impl<'msg> ::protobuf::AsView for UsageSliceView<'msg> {
  type Proxied = UsageSlice;
  fn as_view(&self) -> ::protobuf::View<'msg, UsageSlice> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for UsageSliceView<'msg> {
  fn into_view<'shorter>(self) -> UsageSliceView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<UsageSlice> for UsageSliceView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> UsageSlice {
    let mut dst = UsageSlice::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<UsageSlice> for UsageSliceMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> UsageSlice {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for UsageSlice {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for UsageSliceView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for UsageSliceMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct UsageSliceMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, UsageSlice>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for UsageSliceMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for UsageSliceMut<'msg> {
  type Message = UsageSlice;
}

impl ::std::fmt::Debug for UsageSliceMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, UsageSlice>> for UsageSliceMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, UsageSlice>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> UsageSliceMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, UsageSlice> {
    self.inner
  }

  pub fn to_owned(&self) -> UsageSlice {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // objectSlices: repeated message atom.UsageSlice.MethodUsageSlice
  pub fn objectSlices(&self) -> ::protobuf::RepeatedView<'_, super::usage_slice::MethodUsageSlice> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::usage_slice::MethodUsageSlice>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn objectSlices_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::usage_slice::MethodUsageSlice> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_objectSlices(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::usage_slice::MethodUsageSlice>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

  // userDefinedTypes: repeated message atom.UsageSlice.UserDefinedTypes
  pub fn userDefinedTypes(&self) -> ::protobuf::RepeatedView<'_, super::usage_slice::UserDefinedTypes> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::usage_slice::UserDefinedTypes>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn userDefinedTypes_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::usage_slice::UserDefinedTypes> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_userDefinedTypes(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::usage_slice::UserDefinedTypes>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

}

// SAFETY:
// - `UsageSliceMut` does not perform any shared mutation.
unsafe impl Send for UsageSliceMut<'_> {}

// SAFETY:
// - `UsageSliceMut` does not perform any shared mutation.
unsafe impl Sync for UsageSliceMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for UsageSliceMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for UsageSliceMut<'msg> {}

impl<'msg> ::protobuf::AsView for UsageSliceMut<'msg> {
  type Proxied = UsageSlice;
  fn as_view(&self) -> ::protobuf::View<'_, UsageSlice> {
    UsageSliceView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for UsageSliceMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, UsageSlice>
  where
      'msg: 'shorter {
    UsageSliceView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for UsageSliceMut<'msg> {
  type MutProxied = UsageSlice;
  fn as_mut(&mut self) -> UsageSliceMut<'msg> {
    UsageSliceMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for UsageSliceMut<'msg> {
  fn into_mut<'shorter>(self) -> UsageSliceMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl UsageSlice {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, UsageSlice> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> UsageSliceView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> UsageSliceMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // objectSlices: repeated message atom.UsageSlice.MethodUsageSlice
  pub fn objectSlices(&self) -> ::protobuf::RepeatedView<'_, super::usage_slice::MethodUsageSlice> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::usage_slice::MethodUsageSlice>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn objectSlices_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::usage_slice::MethodUsageSlice> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_objectSlices(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::usage_slice::MethodUsageSlice>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

  // userDefinedTypes: repeated message atom.UsageSlice.UserDefinedTypes
  pub fn userDefinedTypes(&self) -> ::protobuf::RepeatedView<'_, super::usage_slice::UserDefinedTypes> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::usage_slice::UserDefinedTypes>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn userDefinedTypes_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::usage_slice::UserDefinedTypes> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_userDefinedTypes(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::usage_slice::UserDefinedTypes>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

}  // impl UsageSlice

impl ::std::ops::Drop for UsageSlice {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for UsageSlice {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for UsageSlice {
  type Proxied = Self;
  fn as_view(&self) -> UsageSliceView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for UsageSlice {
  type MutProxied = Self;
  fn as_mut(&mut self) -> UsageSliceMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for UsageSlice {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__UsageSlice_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$GG");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__UsageSlice_msg_init.0, &[<super::usage_slice::MethodUsageSlice as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::usage_slice::UserDefinedTypes as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__UsageSlice_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for UsageSlice {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for UsageSlice {
  type Msg = UsageSlice;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<UsageSlice> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for UsageSlice {
  type Msg = UsageSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<UsageSlice> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for UsageSliceMut<'_> {
  type Msg = UsageSlice;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<UsageSlice> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for UsageSliceMut<'_> {
  type Msg = UsageSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<UsageSlice> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for UsageSliceView<'_> {
  type Msg = UsageSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<UsageSlice> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for UsageSliceMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

pub mod usage_slice {// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__UsageSlice__TargetObj_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct TargetObj {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<TargetObj>
}

impl ::protobuf::Message for TargetObj {}

impl ::std::default::Default for TargetObj {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for TargetObj {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `TargetObj` is `Sync` because it does not implement interior mutability.
//    Neither does `TargetObjMut`.
unsafe impl Sync for TargetObj {}

// SAFETY:
// - `TargetObj` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for TargetObj {}

impl ::protobuf::Proxied for TargetObj {
  type View<'msg> = TargetObjView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for TargetObj {}

impl ::protobuf::MutProxied for TargetObj {
  type Mut<'msg> = TargetObjMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct TargetObjView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, TargetObj>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for TargetObjView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for TargetObjView<'msg> {
  type Message = TargetObj;
}

impl ::std::fmt::Debug for TargetObjView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for TargetObjView<'_> {
  fn default() -> TargetObjView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, TargetObj>> for TargetObjView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, TargetObj>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> TargetObjView<'msg> {

  pub fn to_owned(&self) -> TargetObj {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // name: optional string
  pub fn name(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // typeFullName: optional string
  pub fn typeFullName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // position: optional uint32
  pub fn position(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        2, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // isExternal: optional bool
  pub fn isExternal(self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        3, (false).into()
      ).try_into().unwrap()
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // label: optional enum atom.UsageSlice.LabelType
  pub fn label(self) -> super::super::usage_slice::LabelType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        6, (super::super::usage_slice::LabelType::Any).into()
      ).try_into().unwrap()
    }
  }

}

// SAFETY:
// - `TargetObjView` is `Sync` because it does not support mutation.
unsafe impl Sync for TargetObjView<'_> {}

// SAFETY:
// - `TargetObjView` is `Send` because while its alive a `TargetObjMut` cannot.
// - `TargetObjView` does not use thread-local data.
unsafe impl Send for TargetObjView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for TargetObjView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for TargetObjView<'msg> {}

impl<'msg> ::protobuf::AsView for TargetObjView<'msg> {
  type Proxied = TargetObj;
  fn as_view(&self) -> ::protobuf::View<'msg, TargetObj> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for TargetObjView<'msg> {
  fn into_view<'shorter>(self) -> TargetObjView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<TargetObj> for TargetObjView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> TargetObj {
    let mut dst = TargetObj::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<TargetObj> for TargetObjMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> TargetObj {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for TargetObj {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for TargetObjView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for TargetObjMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct TargetObjMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, TargetObj>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for TargetObjMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for TargetObjMut<'msg> {
  type Message = TargetObj;
}

impl ::std::fmt::Debug for TargetObjMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, TargetObj>> for TargetObjMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, TargetObj>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> TargetObjMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, TargetObj> {
    self.inner
  }

  pub fn to_owned(&self) -> TargetObj {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // name: optional string
  pub fn name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // typeFullName: optional string
  pub fn typeFullName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_typeFullName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // position: optional uint32
  pub fn position(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        2, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_position(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        2, val.into()
      )
    }
  }

  // isExternal: optional bool
  pub fn isExternal(&self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        3, (false).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_isExternal(&mut self, val: bool) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_bool_at_index(
        3, val.into()
      )
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        4, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        5, val.into()
      )
    }
  }

  // label: optional enum atom.UsageSlice.LabelType
  pub fn label(&self) -> super::super::usage_slice::LabelType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        6, (super::super::usage_slice::LabelType::Any).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_label(&mut self, val: super::super::usage_slice::LabelType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        6, val.into()
      )
    }
  }

}

// SAFETY:
// - `TargetObjMut` does not perform any shared mutation.
unsafe impl Send for TargetObjMut<'_> {}

// SAFETY:
// - `TargetObjMut` does not perform any shared mutation.
unsafe impl Sync for TargetObjMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for TargetObjMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for TargetObjMut<'msg> {}

impl<'msg> ::protobuf::AsView for TargetObjMut<'msg> {
  type Proxied = TargetObj;
  fn as_view(&self) -> ::protobuf::View<'_, TargetObj> {
    TargetObjView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for TargetObjMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, TargetObj>
  where
      'msg: 'shorter {
    TargetObjView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for TargetObjMut<'msg> {
  type MutProxied = TargetObj;
  fn as_mut(&mut self) -> TargetObjMut<'msg> {
    TargetObjMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for TargetObjMut<'msg> {
  fn into_mut<'shorter>(self) -> TargetObjMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl TargetObj {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, TargetObj> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> TargetObjView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> TargetObjMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // name: optional string
  pub fn name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // typeFullName: optional string
  pub fn typeFullName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_typeFullName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // position: optional uint32
  pub fn position(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        2, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_position(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        2, val.into()
      )
    }
  }

  // isExternal: optional bool
  pub fn isExternal(&self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        3, (false).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_isExternal(&mut self, val: bool) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_bool_at_index(
        3, val.into()
      )
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        4, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        5, val.into()
      )
    }
  }

  // label: optional enum atom.UsageSlice.LabelType
  pub fn label(&self) -> super::super::usage_slice::LabelType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        6, (super::super::usage_slice::LabelType::Any).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_label(&mut self, val: super::super::usage_slice::LabelType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        6, val.into()
      )
    }
  }

}  // impl TargetObj

impl ::std::ops::Drop for TargetObj {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for TargetObj {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for TargetObj {
  type Proxied = Self;
  fn as_view(&self) -> TargetObjView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for TargetObj {
  type MutProxied = Self;
  fn as_mut(&mut self) -> TargetObjMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for TargetObj {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::usage_slice::atom__UsageSlice__TargetObj_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$1X1X)P/P)P)P.P");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::usage_slice::atom__UsageSlice__TargetObj_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::usage_slice::atom__UsageSlice__TargetObj_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for TargetObj {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for TargetObj {
  type Msg = TargetObj;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<TargetObj> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for TargetObj {
  type Msg = TargetObj;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<TargetObj> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for TargetObjMut<'_> {
  type Msg = TargetObj;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<TargetObj> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for TargetObjMut<'_> {
  type Msg = TargetObj;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<TargetObj> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for TargetObjView<'_> {
  type Msg = TargetObj;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<TargetObj> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for TargetObjMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__UsageSlice__DefinedBy_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct DefinedBy {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<DefinedBy>
}

impl ::protobuf::Message for DefinedBy {}

impl ::std::default::Default for DefinedBy {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for DefinedBy {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `DefinedBy` is `Sync` because it does not implement interior mutability.
//    Neither does `DefinedByMut`.
unsafe impl Sync for DefinedBy {}

// SAFETY:
// - `DefinedBy` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for DefinedBy {}

impl ::protobuf::Proxied for DefinedBy {
  type View<'msg> = DefinedByView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for DefinedBy {}

impl ::protobuf::MutProxied for DefinedBy {
  type Mut<'msg> = DefinedByMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct DefinedByView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, DefinedBy>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for DefinedByView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for DefinedByView<'msg> {
  type Message = DefinedBy;
}

impl ::std::fmt::Debug for DefinedByView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for DefinedByView<'_> {
  fn default() -> DefinedByView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, DefinedBy>> for DefinedByView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, DefinedBy>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> DefinedByView<'msg> {

  pub fn to_owned(&self) -> DefinedBy {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // name: optional string
  pub fn name(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // typeFullName: optional string
  pub fn typeFullName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // resolvedMethod: optional string
  pub fn resolvedMethod(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        2, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // position: optional uint32
  pub fn position(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        3, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // isExternal: optional bool
  pub fn isExternal(self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        4, (false).into()
      ).try_into().unwrap()
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        6, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // label: optional string
  pub fn label(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        7, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

}

// SAFETY:
// - `DefinedByView` is `Sync` because it does not support mutation.
unsafe impl Sync for DefinedByView<'_> {}

// SAFETY:
// - `DefinedByView` is `Send` because while its alive a `DefinedByMut` cannot.
// - `DefinedByView` does not use thread-local data.
unsafe impl Send for DefinedByView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for DefinedByView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for DefinedByView<'msg> {}

impl<'msg> ::protobuf::AsView for DefinedByView<'msg> {
  type Proxied = DefinedBy;
  fn as_view(&self) -> ::protobuf::View<'msg, DefinedBy> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for DefinedByView<'msg> {
  fn into_view<'shorter>(self) -> DefinedByView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<DefinedBy> for DefinedByView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> DefinedBy {
    let mut dst = DefinedBy::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<DefinedBy> for DefinedByMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> DefinedBy {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for DefinedBy {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for DefinedByView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for DefinedByMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct DefinedByMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, DefinedBy>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for DefinedByMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for DefinedByMut<'msg> {
  type Message = DefinedBy;
}

impl ::std::fmt::Debug for DefinedByMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, DefinedBy>> for DefinedByMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, DefinedBy>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> DefinedByMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, DefinedBy> {
    self.inner
  }

  pub fn to_owned(&self) -> DefinedBy {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // name: optional string
  pub fn name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // typeFullName: optional string
  pub fn typeFullName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_typeFullName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // resolvedMethod: optional string
  pub fn resolvedMethod(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        2, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_resolvedMethod(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        2,
        view,
      );
    }
  }

  // position: optional uint32
  pub fn position(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        3, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_position(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        3, val.into()
      )
    }
  }

  // isExternal: optional bool
  pub fn isExternal(&self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        4, (false).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_isExternal(&mut self, val: bool) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_bool_at_index(
        4, val.into()
      )
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        5, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        6, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        6, val.into()
      )
    }
  }

  // label: optional string
  pub fn label(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        7, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_label(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        7,
        view,
      );
    }
  }

}

// SAFETY:
// - `DefinedByMut` does not perform any shared mutation.
unsafe impl Send for DefinedByMut<'_> {}

// SAFETY:
// - `DefinedByMut` does not perform any shared mutation.
unsafe impl Sync for DefinedByMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for DefinedByMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for DefinedByMut<'msg> {}

impl<'msg> ::protobuf::AsView for DefinedByMut<'msg> {
  type Proxied = DefinedBy;
  fn as_view(&self) -> ::protobuf::View<'_, DefinedBy> {
    DefinedByView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for DefinedByMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, DefinedBy>
  where
      'msg: 'shorter {
    DefinedByView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for DefinedByMut<'msg> {
  type MutProxied = DefinedBy;
  fn as_mut(&mut self) -> DefinedByMut<'msg> {
    DefinedByMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for DefinedByMut<'msg> {
  fn into_mut<'shorter>(self) -> DefinedByMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl DefinedBy {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, DefinedBy> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> DefinedByView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> DefinedByMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // name: optional string
  pub fn name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // typeFullName: optional string
  pub fn typeFullName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_typeFullName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // resolvedMethod: optional string
  pub fn resolvedMethod(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        2, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_resolvedMethod(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        2,
        view,
      );
    }
  }

  // position: optional uint32
  pub fn position(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        3, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_position(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        3, val.into()
      )
    }
  }

  // isExternal: optional bool
  pub fn isExternal(&self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        4, (false).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_isExternal(&mut self, val: bool) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_bool_at_index(
        4, val.into()
      )
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        5, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        6, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        6, val.into()
      )
    }
  }

  // label: optional string
  pub fn label(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        7, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_label(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        7,
        view,
      );
    }
  }

}  // impl DefinedBy

impl ::std::ops::Drop for DefinedBy {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for DefinedBy {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for DefinedBy {
  type Proxied = Self;
  fn as_view(&self) -> DefinedByView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for DefinedBy {
  type MutProxied = Self;
  fn as_mut(&mut self) -> DefinedByMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for DefinedBy {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::usage_slice::atom__UsageSlice__DefinedBy_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$1X1X1X)P/P)P)P1X");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::usage_slice::atom__UsageSlice__DefinedBy_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::usage_slice::atom__UsageSlice__DefinedBy_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for DefinedBy {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for DefinedBy {
  type Msg = DefinedBy;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DefinedBy> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for DefinedBy {
  type Msg = DefinedBy;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DefinedBy> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for DefinedByMut<'_> {
  type Msg = DefinedBy;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DefinedBy> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for DefinedByMut<'_> {
  type Msg = DefinedBy;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DefinedBy> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for DefinedByView<'_> {
  type Msg = DefinedBy;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DefinedBy> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for DefinedByMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__UsageSlice__InvokedCalls_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct InvokedCalls {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<InvokedCalls>
}

impl ::protobuf::Message for InvokedCalls {}

impl ::std::default::Default for InvokedCalls {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for InvokedCalls {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `InvokedCalls` is `Sync` because it does not implement interior mutability.
//    Neither does `InvokedCallsMut`.
unsafe impl Sync for InvokedCalls {}

// SAFETY:
// - `InvokedCalls` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for InvokedCalls {}

impl ::protobuf::Proxied for InvokedCalls {
  type View<'msg> = InvokedCallsView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for InvokedCalls {}

impl ::protobuf::MutProxied for InvokedCalls {
  type Mut<'msg> = InvokedCallsMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct InvokedCallsView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, InvokedCalls>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for InvokedCallsView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for InvokedCallsView<'msg> {
  type Message = InvokedCalls;
}

impl ::std::fmt::Debug for InvokedCallsView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for InvokedCallsView<'_> {
  fn default() -> InvokedCallsView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, InvokedCalls>> for InvokedCallsView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, InvokedCalls>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> InvokedCallsView<'msg> {

  pub fn to_owned(&self) -> InvokedCalls {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // callName: optional string
  pub fn callName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // resolvedMethod: optional string
  pub fn resolvedMethod(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // paramTypes: repeated string
  pub fn paramTypes(self) -> ::protobuf::RepeatedView<'msg, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // returnType: optional string
  pub fn returnType(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // isExternal: optional bool
  pub fn isExternal(self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        4, (false).into()
      ).try_into().unwrap()
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        6, (0u32).into()
      ).try_into().unwrap()
    }
  }

}

// SAFETY:
// - `InvokedCallsView` is `Sync` because it does not support mutation.
unsafe impl Sync for InvokedCallsView<'_> {}

// SAFETY:
// - `InvokedCallsView` is `Send` because while its alive a `InvokedCallsMut` cannot.
// - `InvokedCallsView` does not use thread-local data.
unsafe impl Send for InvokedCallsView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for InvokedCallsView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for InvokedCallsView<'msg> {}

impl<'msg> ::protobuf::AsView for InvokedCallsView<'msg> {
  type Proxied = InvokedCalls;
  fn as_view(&self) -> ::protobuf::View<'msg, InvokedCalls> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for InvokedCallsView<'msg> {
  fn into_view<'shorter>(self) -> InvokedCallsView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<InvokedCalls> for InvokedCallsView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> InvokedCalls {
    let mut dst = InvokedCalls::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<InvokedCalls> for InvokedCallsMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> InvokedCalls {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for InvokedCalls {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for InvokedCallsView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for InvokedCallsMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct InvokedCallsMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, InvokedCalls>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for InvokedCallsMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for InvokedCallsMut<'msg> {
  type Message = InvokedCalls;
}

impl ::std::fmt::Debug for InvokedCallsMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, InvokedCalls>> for InvokedCallsMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, InvokedCalls>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> InvokedCallsMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, InvokedCalls> {
    self.inner
  }

  pub fn to_owned(&self) -> InvokedCalls {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // callName: optional string
  pub fn callName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_callName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // resolvedMethod: optional string
  pub fn resolvedMethod(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_resolvedMethod(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // paramTypes: repeated string
  pub fn paramTypes(&self) -> ::protobuf::RepeatedView<'_, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn paramTypes_mut(&mut self) -> ::protobuf::RepeatedMut<'_, ::protobuf::ProtoString> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_paramTypes(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<::protobuf::ProtoString>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

  // returnType: optional string
  pub fn returnType(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_returnType(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

  // isExternal: optional bool
  pub fn isExternal(&self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        4, (false).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_isExternal(&mut self, val: bool) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_bool_at_index(
        4, val.into()
      )
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        5, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        6, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        6, val.into()
      )
    }
  }

}

// SAFETY:
// - `InvokedCallsMut` does not perform any shared mutation.
unsafe impl Send for InvokedCallsMut<'_> {}

// SAFETY:
// - `InvokedCallsMut` does not perform any shared mutation.
unsafe impl Sync for InvokedCallsMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for InvokedCallsMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for InvokedCallsMut<'msg> {}

impl<'msg> ::protobuf::AsView for InvokedCallsMut<'msg> {
  type Proxied = InvokedCalls;
  fn as_view(&self) -> ::protobuf::View<'_, InvokedCalls> {
    InvokedCallsView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for InvokedCallsMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, InvokedCalls>
  where
      'msg: 'shorter {
    InvokedCallsView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for InvokedCallsMut<'msg> {
  type MutProxied = InvokedCalls;
  fn as_mut(&mut self) -> InvokedCallsMut<'msg> {
    InvokedCallsMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for InvokedCallsMut<'msg> {
  fn into_mut<'shorter>(self) -> InvokedCallsMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl InvokedCalls {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, InvokedCalls> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> InvokedCallsView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> InvokedCallsMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // callName: optional string
  pub fn callName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_callName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // resolvedMethod: optional string
  pub fn resolvedMethod(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_resolvedMethod(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // paramTypes: repeated string
  pub fn paramTypes(&self) -> ::protobuf::RepeatedView<'_, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn paramTypes_mut(&mut self) -> ::protobuf::RepeatedMut<'_, ::protobuf::ProtoString> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_paramTypes(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<::protobuf::ProtoString>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

  // returnType: optional string
  pub fn returnType(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_returnType(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

  // isExternal: optional bool
  pub fn isExternal(&self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        4, (false).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_isExternal(&mut self, val: bool) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_bool_at_index(
        4, val.into()
      )
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        5, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        6, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        6, val.into()
      )
    }
  }

}  // impl InvokedCalls

impl ::std::ops::Drop for InvokedCalls {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for InvokedCalls {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for InvokedCalls {
  type Proxied = Self;
  fn as_view(&self) -> InvokedCallsView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for InvokedCalls {
  type MutProxied = Self;
  fn as_mut(&mut self) -> InvokedCallsMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for InvokedCalls {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::usage_slice::atom__UsageSlice__InvokedCalls_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$1X1XET1X/P)P)P");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::usage_slice::atom__UsageSlice__InvokedCalls_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::usage_slice::atom__UsageSlice__InvokedCalls_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for InvokedCalls {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for InvokedCalls {
  type Msg = InvokedCalls;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<InvokedCalls> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for InvokedCalls {
  type Msg = InvokedCalls;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<InvokedCalls> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for InvokedCallsMut<'_> {
  type Msg = InvokedCalls;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<InvokedCalls> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for InvokedCallsMut<'_> {
  type Msg = InvokedCalls;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<InvokedCalls> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for InvokedCallsView<'_> {
  type Msg = InvokedCalls;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<InvokedCalls> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for InvokedCallsMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__UsageSlice__ArgToCalls_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct ArgToCalls {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<ArgToCalls>
}

impl ::protobuf::Message for ArgToCalls {}

impl ::std::default::Default for ArgToCalls {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for ArgToCalls {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `ArgToCalls` is `Sync` because it does not implement interior mutability.
//    Neither does `ArgToCallsMut`.
unsafe impl Sync for ArgToCalls {}

// SAFETY:
// - `ArgToCalls` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for ArgToCalls {}

impl ::protobuf::Proxied for ArgToCalls {
  type View<'msg> = ArgToCallsView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for ArgToCalls {}

impl ::protobuf::MutProxied for ArgToCalls {
  type Mut<'msg> = ArgToCallsMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct ArgToCallsView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, ArgToCalls>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for ArgToCallsView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for ArgToCallsView<'msg> {
  type Message = ArgToCalls;
}

impl ::std::fmt::Debug for ArgToCallsView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for ArgToCallsView<'_> {
  fn default() -> ArgToCallsView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, ArgToCalls>> for ArgToCallsView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, ArgToCalls>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> ArgToCallsView<'msg> {

  pub fn to_owned(&self) -> ArgToCalls {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // callName: optional string
  pub fn callName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // resolvedMethod: optional string
  pub fn resolvedMethod(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // paramTypes: repeated string
  pub fn paramTypes(self) -> ::protobuf::RepeatedView<'msg, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // returnType: optional string
  pub fn returnType(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // position: optional uint32
  pub fn position(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // isExternal: optional bool
  pub fn isExternal(self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        5, (false).into()
      ).try_into().unwrap()
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        6, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        7, (0u32).into()
      ).try_into().unwrap()
    }
  }

}

// SAFETY:
// - `ArgToCallsView` is `Sync` because it does not support mutation.
unsafe impl Sync for ArgToCallsView<'_> {}

// SAFETY:
// - `ArgToCallsView` is `Send` because while its alive a `ArgToCallsMut` cannot.
// - `ArgToCallsView` does not use thread-local data.
unsafe impl Send for ArgToCallsView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for ArgToCallsView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for ArgToCallsView<'msg> {}

impl<'msg> ::protobuf::AsView for ArgToCallsView<'msg> {
  type Proxied = ArgToCalls;
  fn as_view(&self) -> ::protobuf::View<'msg, ArgToCalls> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ArgToCallsView<'msg> {
  fn into_view<'shorter>(self) -> ArgToCallsView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<ArgToCalls> for ArgToCallsView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> ArgToCalls {
    let mut dst = ArgToCalls::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<ArgToCalls> for ArgToCallsMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> ArgToCalls {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for ArgToCalls {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for ArgToCallsView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for ArgToCallsMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct ArgToCallsMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, ArgToCalls>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for ArgToCallsMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for ArgToCallsMut<'msg> {
  type Message = ArgToCalls;
}

impl ::std::fmt::Debug for ArgToCallsMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, ArgToCalls>> for ArgToCallsMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, ArgToCalls>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> ArgToCallsMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, ArgToCalls> {
    self.inner
  }

  pub fn to_owned(&self) -> ArgToCalls {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // callName: optional string
  pub fn callName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_callName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // resolvedMethod: optional string
  pub fn resolvedMethod(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_resolvedMethod(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // paramTypes: repeated string
  pub fn paramTypes(&self) -> ::protobuf::RepeatedView<'_, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn paramTypes_mut(&mut self) -> ::protobuf::RepeatedMut<'_, ::protobuf::ProtoString> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_paramTypes(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<::protobuf::ProtoString>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

  // returnType: optional string
  pub fn returnType(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_returnType(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

  // position: optional uint32
  pub fn position(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_position(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        4, val.into()
      )
    }
  }

  // isExternal: optional bool
  pub fn isExternal(&self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        5, (false).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_isExternal(&mut self, val: bool) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_bool_at_index(
        5, val.into()
      )
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        6, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        6, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        7, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        7, val.into()
      )
    }
  }

}

// SAFETY:
// - `ArgToCallsMut` does not perform any shared mutation.
unsafe impl Send for ArgToCallsMut<'_> {}

// SAFETY:
// - `ArgToCallsMut` does not perform any shared mutation.
unsafe impl Sync for ArgToCallsMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for ArgToCallsMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for ArgToCallsMut<'msg> {}

impl<'msg> ::protobuf::AsView for ArgToCallsMut<'msg> {
  type Proxied = ArgToCalls;
  fn as_view(&self) -> ::protobuf::View<'_, ArgToCalls> {
    ArgToCallsView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ArgToCallsMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, ArgToCalls>
  where
      'msg: 'shorter {
    ArgToCallsView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for ArgToCallsMut<'msg> {
  type MutProxied = ArgToCalls;
  fn as_mut(&mut self) -> ArgToCallsMut<'msg> {
    ArgToCallsMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for ArgToCallsMut<'msg> {
  fn into_mut<'shorter>(self) -> ArgToCallsMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl ArgToCalls {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, ArgToCalls> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> ArgToCallsView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> ArgToCallsMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // callName: optional string
  pub fn callName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_callName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // resolvedMethod: optional string
  pub fn resolvedMethod(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_resolvedMethod(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // paramTypes: repeated string
  pub fn paramTypes(&self) -> ::protobuf::RepeatedView<'_, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn paramTypes_mut(&mut self) -> ::protobuf::RepeatedMut<'_, ::protobuf::ProtoString> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_paramTypes(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<::protobuf::ProtoString>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

  // returnType: optional string
  pub fn returnType(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_returnType(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

  // position: optional uint32
  pub fn position(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_position(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        4, val.into()
      )
    }
  }

  // isExternal: optional bool
  pub fn isExternal(&self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        5, (false).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_isExternal(&mut self, val: bool) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_bool_at_index(
        5, val.into()
      )
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        6, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        6, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        7, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        7, val.into()
      )
    }
  }

}  // impl ArgToCalls

impl ::std::ops::Drop for ArgToCalls {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for ArgToCalls {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for ArgToCalls {
  type Proxied = Self;
  fn as_view(&self) -> ArgToCallsView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for ArgToCalls {
  type MutProxied = Self;
  fn as_mut(&mut self) -> ArgToCallsMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for ArgToCalls {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::usage_slice::atom__UsageSlice__ArgToCalls_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$1X1XET1X)P/P)P)P");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::usage_slice::atom__UsageSlice__ArgToCalls_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::usage_slice::atom__UsageSlice__ArgToCalls_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for ArgToCalls {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for ArgToCalls {
  type Msg = ArgToCalls;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ArgToCalls> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ArgToCalls {
  type Msg = ArgToCalls;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ArgToCalls> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for ArgToCallsMut<'_> {
  type Msg = ArgToCalls;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ArgToCalls> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ArgToCallsMut<'_> {
  type Msg = ArgToCalls;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ArgToCalls> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ArgToCallsView<'_> {
  type Msg = ArgToCalls;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ArgToCalls> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for ArgToCallsMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__UsageSlice__ObjectUsageSlice_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct ObjectUsageSlice {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<ObjectUsageSlice>
}

impl ::protobuf::Message for ObjectUsageSlice {}

impl ::std::default::Default for ObjectUsageSlice {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for ObjectUsageSlice {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `ObjectUsageSlice` is `Sync` because it does not implement interior mutability.
//    Neither does `ObjectUsageSliceMut`.
unsafe impl Sync for ObjectUsageSlice {}

// SAFETY:
// - `ObjectUsageSlice` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for ObjectUsageSlice {}

impl ::protobuf::Proxied for ObjectUsageSlice {
  type View<'msg> = ObjectUsageSliceView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for ObjectUsageSlice {}

impl ::protobuf::MutProxied for ObjectUsageSlice {
  type Mut<'msg> = ObjectUsageSliceMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct ObjectUsageSliceView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, ObjectUsageSlice>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for ObjectUsageSliceView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for ObjectUsageSliceView<'msg> {
  type Message = ObjectUsageSlice;
}

impl ::std::fmt::Debug for ObjectUsageSliceView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for ObjectUsageSliceView<'_> {
  fn default() -> ObjectUsageSliceView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, ObjectUsageSlice>> for ObjectUsageSliceView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, ObjectUsageSlice>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> ObjectUsageSliceView<'msg> {

  pub fn to_owned(&self) -> ObjectUsageSlice {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // targetObj: optional message atom.UsageSlice.TargetObj
  pub fn has_targetObj(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(0)
    }
  }
  pub fn targetObj_opt(self) -> ::protobuf::Optional<super::super::usage_slice::TargetObjView<'msg>> {
        ::protobuf::Optional::new(self.targetObj(), self.has_targetObj())
  }
  pub fn targetObj(self) -> super::super::usage_slice::TargetObjView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(0)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::usage_slice::TargetObjView::default())
  }

  // definedBy: optional message atom.UsageSlice.DefinedBy
  pub fn has_definedBy(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn definedBy_opt(self) -> ::protobuf::Optional<super::super::usage_slice::DefinedByView<'msg>> {
        ::protobuf::Optional::new(self.definedBy(), self.has_definedBy())
  }
  pub fn definedBy(self) -> super::super::usage_slice::DefinedByView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::usage_slice::DefinedByView::default())
  }

  // invokedCalls: repeated message atom.UsageSlice.InvokedCalls
  pub fn invokedCalls(self) -> ::protobuf::RepeatedView<'msg, super::super::usage_slice::InvokedCalls> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::InvokedCalls>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // argToCalls: repeated message atom.UsageSlice.ArgToCalls
  pub fn argToCalls(self) -> ::protobuf::RepeatedView<'msg, super::super::usage_slice::ArgToCalls> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        3
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::ArgToCalls>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `ObjectUsageSliceView` is `Sync` because it does not support mutation.
unsafe impl Sync for ObjectUsageSliceView<'_> {}

// SAFETY:
// - `ObjectUsageSliceView` is `Send` because while its alive a `ObjectUsageSliceMut` cannot.
// - `ObjectUsageSliceView` does not use thread-local data.
unsafe impl Send for ObjectUsageSliceView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for ObjectUsageSliceView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for ObjectUsageSliceView<'msg> {}

impl<'msg> ::protobuf::AsView for ObjectUsageSliceView<'msg> {
  type Proxied = ObjectUsageSlice;
  fn as_view(&self) -> ::protobuf::View<'msg, ObjectUsageSlice> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ObjectUsageSliceView<'msg> {
  fn into_view<'shorter>(self) -> ObjectUsageSliceView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<ObjectUsageSlice> for ObjectUsageSliceView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> ObjectUsageSlice {
    let mut dst = ObjectUsageSlice::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<ObjectUsageSlice> for ObjectUsageSliceMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> ObjectUsageSlice {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for ObjectUsageSlice {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for ObjectUsageSliceView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for ObjectUsageSliceMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct ObjectUsageSliceMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, ObjectUsageSlice>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for ObjectUsageSliceMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for ObjectUsageSliceMut<'msg> {
  type Message = ObjectUsageSlice;
}

impl ::std::fmt::Debug for ObjectUsageSliceMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, ObjectUsageSlice>> for ObjectUsageSliceMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, ObjectUsageSlice>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> ObjectUsageSliceMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, ObjectUsageSlice> {
    self.inner
  }

  pub fn to_owned(&self) -> ObjectUsageSlice {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // targetObj: optional message atom.UsageSlice.TargetObj
  pub fn has_targetObj(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(0)
    }
  }
  pub fn clear_targetObj(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        0
      );
    }
  }
  pub fn targetObj_opt(&self) -> ::protobuf::Optional<super::super::usage_slice::TargetObjView<'_>> {
        ::protobuf::Optional::new(self.targetObj(), self.has_targetObj())
  }
  pub fn targetObj(&self) -> super::super::usage_slice::TargetObjView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(0)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::usage_slice::TargetObjView::default())
  }
  pub fn targetObj_mut(&mut self) -> super::super::usage_slice::TargetObjMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         0, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_targetObj(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::usage_slice::TargetObj>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        0,
        val
      );
    }
  }

  // definedBy: optional message atom.UsageSlice.DefinedBy
  pub fn has_definedBy(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_definedBy(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn definedBy_opt(&self) -> ::protobuf::Optional<super::super::usage_slice::DefinedByView<'_>> {
        ::protobuf::Optional::new(self.definedBy(), self.has_definedBy())
  }
  pub fn definedBy(&self) -> super::super::usage_slice::DefinedByView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::usage_slice::DefinedByView::default())
  }
  pub fn definedBy_mut(&mut self) -> super::super::usage_slice::DefinedByMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_definedBy(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::usage_slice::DefinedBy>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

  // invokedCalls: repeated message atom.UsageSlice.InvokedCalls
  pub fn invokedCalls(&self) -> ::protobuf::RepeatedView<'_, super::super::usage_slice::InvokedCalls> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::InvokedCalls>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn invokedCalls_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::usage_slice::InvokedCalls> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_invokedCalls(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::usage_slice::InvokedCalls>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

  // argToCalls: repeated message atom.UsageSlice.ArgToCalls
  pub fn argToCalls(&self) -> ::protobuf::RepeatedView<'_, super::super::usage_slice::ArgToCalls> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        3
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::ArgToCalls>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn argToCalls_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::usage_slice::ArgToCalls> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        3,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_argToCalls(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::usage_slice::ArgToCalls>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                3,
                inner.raw());
    }
  }

}

// SAFETY:
// - `ObjectUsageSliceMut` does not perform any shared mutation.
unsafe impl Send for ObjectUsageSliceMut<'_> {}

// SAFETY:
// - `ObjectUsageSliceMut` does not perform any shared mutation.
unsafe impl Sync for ObjectUsageSliceMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for ObjectUsageSliceMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for ObjectUsageSliceMut<'msg> {}

impl<'msg> ::protobuf::AsView for ObjectUsageSliceMut<'msg> {
  type Proxied = ObjectUsageSlice;
  fn as_view(&self) -> ::protobuf::View<'_, ObjectUsageSlice> {
    ObjectUsageSliceView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ObjectUsageSliceMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, ObjectUsageSlice>
  where
      'msg: 'shorter {
    ObjectUsageSliceView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for ObjectUsageSliceMut<'msg> {
  type MutProxied = ObjectUsageSlice;
  fn as_mut(&mut self) -> ObjectUsageSliceMut<'msg> {
    ObjectUsageSliceMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for ObjectUsageSliceMut<'msg> {
  fn into_mut<'shorter>(self) -> ObjectUsageSliceMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl ObjectUsageSlice {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, ObjectUsageSlice> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> ObjectUsageSliceView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> ObjectUsageSliceMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // targetObj: optional message atom.UsageSlice.TargetObj
  pub fn has_targetObj(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(0)
    }
  }
  pub fn clear_targetObj(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        0
      );
    }
  }
  pub fn targetObj_opt(&self) -> ::protobuf::Optional<super::super::usage_slice::TargetObjView<'_>> {
        ::protobuf::Optional::new(self.targetObj(), self.has_targetObj())
  }
  pub fn targetObj(&self) -> super::super::usage_slice::TargetObjView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(0)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::usage_slice::TargetObjView::default())
  }
  pub fn targetObj_mut(&mut self) -> super::super::usage_slice::TargetObjMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         0, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_targetObj(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::usage_slice::TargetObj>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        0,
        val
      );
    }
  }

  // definedBy: optional message atom.UsageSlice.DefinedBy
  pub fn has_definedBy(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_definedBy(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn definedBy_opt(&self) -> ::protobuf::Optional<super::super::usage_slice::DefinedByView<'_>> {
        ::protobuf::Optional::new(self.definedBy(), self.has_definedBy())
  }
  pub fn definedBy(&self) -> super::super::usage_slice::DefinedByView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::super::usage_slice::DefinedByView::default())
  }
  pub fn definedBy_mut(&mut self) -> super::super::usage_slice::DefinedByMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_definedBy(&mut self,
    val: impl ::protobuf::IntoProxied<super::super::usage_slice::DefinedBy>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

  // invokedCalls: repeated message atom.UsageSlice.InvokedCalls
  pub fn invokedCalls(&self) -> ::protobuf::RepeatedView<'_, super::super::usage_slice::InvokedCalls> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::InvokedCalls>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn invokedCalls_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::usage_slice::InvokedCalls> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_invokedCalls(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::usage_slice::InvokedCalls>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

  // argToCalls: repeated message atom.UsageSlice.ArgToCalls
  pub fn argToCalls(&self) -> ::protobuf::RepeatedView<'_, super::super::usage_slice::ArgToCalls> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        3
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::ArgToCalls>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn argToCalls_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::usage_slice::ArgToCalls> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        3,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_argToCalls(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::usage_slice::ArgToCalls>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                3,
                inner.raw());
    }
  }

}  // impl ObjectUsageSlice

impl ::std::ops::Drop for ObjectUsageSlice {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for ObjectUsageSlice {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for ObjectUsageSlice {
  type Proxied = Self;
  fn as_view(&self) -> ObjectUsageSliceView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for ObjectUsageSlice {
  type MutProxied = Self;
  fn as_mut(&mut self) -> ObjectUsageSliceMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for ObjectUsageSlice {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::usage_slice::atom__UsageSlice__ObjectUsageSlice_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$33GG");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::usage_slice::atom__UsageSlice__ObjectUsageSlice_msg_init.0, &[<super::super::usage_slice::TargetObj as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::super::usage_slice::DefinedBy as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::super::usage_slice::InvokedCalls as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::super::usage_slice::ArgToCalls as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::usage_slice::atom__UsageSlice__ObjectUsageSlice_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for ObjectUsageSlice {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for ObjectUsageSlice {
  type Msg = ObjectUsageSlice;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ObjectUsageSlice> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ObjectUsageSlice {
  type Msg = ObjectUsageSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ObjectUsageSlice> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for ObjectUsageSliceMut<'_> {
  type Msg = ObjectUsageSlice;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ObjectUsageSlice> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ObjectUsageSliceMut<'_> {
  type Msg = ObjectUsageSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ObjectUsageSlice> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ObjectUsageSliceView<'_> {
  type Msg = ObjectUsageSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ObjectUsageSlice> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for ObjectUsageSliceMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__UsageSlice__MethodUsageSlice_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct MethodUsageSlice {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<MethodUsageSlice>
}

impl ::protobuf::Message for MethodUsageSlice {}

impl ::std::default::Default for MethodUsageSlice {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for MethodUsageSlice {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `MethodUsageSlice` is `Sync` because it does not implement interior mutability.
//    Neither does `MethodUsageSliceMut`.
unsafe impl Sync for MethodUsageSlice {}

// SAFETY:
// - `MethodUsageSlice` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for MethodUsageSlice {}

impl ::protobuf::Proxied for MethodUsageSlice {
  type View<'msg> = MethodUsageSliceView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for MethodUsageSlice {}

impl ::protobuf::MutProxied for MethodUsageSlice {
  type Mut<'msg> = MethodUsageSliceMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct MethodUsageSliceView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, MethodUsageSlice>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for MethodUsageSliceView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for MethodUsageSliceView<'msg> {
  type Message = MethodUsageSlice;
}

impl ::std::fmt::Debug for MethodUsageSliceView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for MethodUsageSliceView<'_> {
  fn default() -> MethodUsageSliceView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, MethodUsageSlice>> for MethodUsageSliceView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, MethodUsageSlice>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> MethodUsageSliceView<'msg> {

  pub fn to_owned(&self) -> MethodUsageSlice {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // code: optional string
  pub fn code(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // fullName: optional string
  pub fn fullName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // fileName: optional string
  pub fn fileName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        2, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        3, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // usages: repeated message atom.UsageSlice.ObjectUsageSlice
  pub fn usages(self) -> ::protobuf::RepeatedView<'msg, super::super::usage_slice::ObjectUsageSlice> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        5
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::ObjectUsageSlice>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `MethodUsageSliceView` is `Sync` because it does not support mutation.
unsafe impl Sync for MethodUsageSliceView<'_> {}

// SAFETY:
// - `MethodUsageSliceView` is `Send` because while its alive a `MethodUsageSliceMut` cannot.
// - `MethodUsageSliceView` does not use thread-local data.
unsafe impl Send for MethodUsageSliceView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for MethodUsageSliceView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for MethodUsageSliceView<'msg> {}

impl<'msg> ::protobuf::AsView for MethodUsageSliceView<'msg> {
  type Proxied = MethodUsageSlice;
  fn as_view(&self) -> ::protobuf::View<'msg, MethodUsageSlice> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for MethodUsageSliceView<'msg> {
  fn into_view<'shorter>(self) -> MethodUsageSliceView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<MethodUsageSlice> for MethodUsageSliceView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> MethodUsageSlice {
    let mut dst = MethodUsageSlice::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<MethodUsageSlice> for MethodUsageSliceMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> MethodUsageSlice {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for MethodUsageSlice {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for MethodUsageSliceView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for MethodUsageSliceMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct MethodUsageSliceMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, MethodUsageSlice>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for MethodUsageSliceMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for MethodUsageSliceMut<'msg> {
  type Message = MethodUsageSlice;
}

impl ::std::fmt::Debug for MethodUsageSliceMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, MethodUsageSlice>> for MethodUsageSliceMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, MethodUsageSlice>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> MethodUsageSliceMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, MethodUsageSlice> {
    self.inner
  }

  pub fn to_owned(&self) -> MethodUsageSlice {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // code: optional string
  pub fn code(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_code(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // fullName: optional string
  pub fn fullName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_fullName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // fileName: optional string
  pub fn fileName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        2, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_fileName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        2,
        view,
      );
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        3, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        3, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        4, val.into()
      )
    }
  }

  // usages: repeated message atom.UsageSlice.ObjectUsageSlice
  pub fn usages(&self) -> ::protobuf::RepeatedView<'_, super::super::usage_slice::ObjectUsageSlice> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        5
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::ObjectUsageSlice>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn usages_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::usage_slice::ObjectUsageSlice> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        5,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_usages(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::usage_slice::ObjectUsageSlice>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                5,
                inner.raw());
    }
  }

}

// SAFETY:
// - `MethodUsageSliceMut` does not perform any shared mutation.
unsafe impl Send for MethodUsageSliceMut<'_> {}

// SAFETY:
// - `MethodUsageSliceMut` does not perform any shared mutation.
unsafe impl Sync for MethodUsageSliceMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for MethodUsageSliceMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for MethodUsageSliceMut<'msg> {}

impl<'msg> ::protobuf::AsView for MethodUsageSliceMut<'msg> {
  type Proxied = MethodUsageSlice;
  fn as_view(&self) -> ::protobuf::View<'_, MethodUsageSlice> {
    MethodUsageSliceView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for MethodUsageSliceMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, MethodUsageSlice>
  where
      'msg: 'shorter {
    MethodUsageSliceView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for MethodUsageSliceMut<'msg> {
  type MutProxied = MethodUsageSlice;
  fn as_mut(&mut self) -> MethodUsageSliceMut<'msg> {
    MethodUsageSliceMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for MethodUsageSliceMut<'msg> {
  fn into_mut<'shorter>(self) -> MethodUsageSliceMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl MethodUsageSlice {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, MethodUsageSlice> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> MethodUsageSliceView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> MethodUsageSliceMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // code: optional string
  pub fn code(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_code(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // fullName: optional string
  pub fn fullName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_fullName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // fileName: optional string
  pub fn fileName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        2, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_fileName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        2,
        view,
      );
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        3, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        3, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        4, val.into()
      )
    }
  }

  // usages: repeated message atom.UsageSlice.ObjectUsageSlice
  pub fn usages(&self) -> ::protobuf::RepeatedView<'_, super::super::usage_slice::ObjectUsageSlice> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        5
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::ObjectUsageSlice>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn usages_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::usage_slice::ObjectUsageSlice> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        5,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_usages(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::usage_slice::ObjectUsageSlice>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                5,
                inner.raw());
    }
  }

}  // impl MethodUsageSlice

impl ::std::ops::Drop for MethodUsageSlice {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for MethodUsageSlice {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for MethodUsageSlice {
  type Proxied = Self;
  fn as_view(&self) -> MethodUsageSliceView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for MethodUsageSlice {
  type MutProxied = Self;
  fn as_mut(&mut self) -> MethodUsageSliceMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for MethodUsageSlice {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::usage_slice::atom__UsageSlice__MethodUsageSlice_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$1X1X1X)P)PG");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::usage_slice::atom__UsageSlice__MethodUsageSlice_msg_init.0, &[<super::super::usage_slice::ObjectUsageSlice as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::usage_slice::atom__UsageSlice__MethodUsageSlice_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for MethodUsageSlice {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for MethodUsageSlice {
  type Msg = MethodUsageSlice;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<MethodUsageSlice> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for MethodUsageSlice {
  type Msg = MethodUsageSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<MethodUsageSlice> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for MethodUsageSliceMut<'_> {
  type Msg = MethodUsageSlice;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<MethodUsageSlice> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for MethodUsageSliceMut<'_> {
  type Msg = MethodUsageSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<MethodUsageSlice> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for MethodUsageSliceView<'_> {
  type Msg = MethodUsageSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<MethodUsageSlice> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for MethodUsageSliceMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__UsageSlice__Fields_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct Fields {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<Fields>
}

impl ::protobuf::Message for Fields {}

impl ::std::default::Default for Fields {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for Fields {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `Fields` is `Sync` because it does not implement interior mutability.
//    Neither does `FieldsMut`.
unsafe impl Sync for Fields {}

// SAFETY:
// - `Fields` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for Fields {}

impl ::protobuf::Proxied for Fields {
  type View<'msg> = FieldsView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for Fields {}

impl ::protobuf::MutProxied for Fields {
  type Mut<'msg> = FieldsMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct FieldsView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Fields>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for FieldsView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for FieldsView<'msg> {
  type Message = Fields;
}

impl ::std::fmt::Debug for FieldsView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for FieldsView<'_> {
  fn default() -> FieldsView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, Fields>> for FieldsView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Fields>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> FieldsView<'msg> {

  pub fn to_owned(&self) -> Fields {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // name: optional string
  pub fn name(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // typeFullName: optional string
  pub fn typeFullName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        2, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        3, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // label: optional enum atom.UsageSlice.LabelType
  pub fn label(self) -> super::super::usage_slice::LabelType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        4, (super::super::usage_slice::LabelType::Any).into()
      ).try_into().unwrap()
    }
  }

}

// SAFETY:
// - `FieldsView` is `Sync` because it does not support mutation.
unsafe impl Sync for FieldsView<'_> {}

// SAFETY:
// - `FieldsView` is `Send` because while its alive a `FieldsMut` cannot.
// - `FieldsView` does not use thread-local data.
unsafe impl Send for FieldsView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for FieldsView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for FieldsView<'msg> {}

impl<'msg> ::protobuf::AsView for FieldsView<'msg> {
  type Proxied = Fields;
  fn as_view(&self) -> ::protobuf::View<'msg, Fields> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for FieldsView<'msg> {
  fn into_view<'shorter>(self) -> FieldsView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<Fields> for FieldsView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Fields {
    let mut dst = Fields::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<Fields> for FieldsMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Fields {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for Fields {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for FieldsView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for FieldsMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct FieldsMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Fields>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for FieldsMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for FieldsMut<'msg> {
  type Message = Fields;
}

impl ::std::fmt::Debug for FieldsMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, Fields>> for FieldsMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Fields>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> FieldsMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, Fields> {
    self.inner
  }

  pub fn to_owned(&self) -> Fields {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // name: optional string
  pub fn name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // typeFullName: optional string
  pub fn typeFullName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_typeFullName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        2, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        2, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        3, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        3, val.into()
      )
    }
  }

  // label: optional enum atom.UsageSlice.LabelType
  pub fn label(&self) -> super::super::usage_slice::LabelType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        4, (super::super::usage_slice::LabelType::Any).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_label(&mut self, val: super::super::usage_slice::LabelType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        4, val.into()
      )
    }
  }

}

// SAFETY:
// - `FieldsMut` does not perform any shared mutation.
unsafe impl Send for FieldsMut<'_> {}

// SAFETY:
// - `FieldsMut` does not perform any shared mutation.
unsafe impl Sync for FieldsMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for FieldsMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for FieldsMut<'msg> {}

impl<'msg> ::protobuf::AsView for FieldsMut<'msg> {
  type Proxied = Fields;
  fn as_view(&self) -> ::protobuf::View<'_, Fields> {
    FieldsView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for FieldsMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, Fields>
  where
      'msg: 'shorter {
    FieldsView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for FieldsMut<'msg> {
  type MutProxied = Fields;
  fn as_mut(&mut self) -> FieldsMut<'msg> {
    FieldsMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for FieldsMut<'msg> {
  fn into_mut<'shorter>(self) -> FieldsMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl Fields {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, Fields> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> FieldsView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> FieldsMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // name: optional string
  pub fn name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // typeFullName: optional string
  pub fn typeFullName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_typeFullName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        2, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        2, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        3, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        3, val.into()
      )
    }
  }

  // label: optional enum atom.UsageSlice.LabelType
  pub fn label(&self) -> super::super::usage_slice::LabelType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        4, (super::super::usage_slice::LabelType::Any).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_label(&mut self, val: super::super::usage_slice::LabelType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        4, val.into()
      )
    }
  }

}  // impl Fields

impl ::std::ops::Drop for Fields {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for Fields {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for Fields {
  type Proxied = Self;
  fn as_view(&self) -> FieldsView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for Fields {
  type MutProxied = Self;
  fn as_mut(&mut self) -> FieldsMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for Fields {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::usage_slice::atom__UsageSlice__Fields_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$1X1X)P)P.P");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::usage_slice::atom__UsageSlice__Fields_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::usage_slice::atom__UsageSlice__Fields_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for Fields {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for Fields {
  type Msg = Fields;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Fields> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for Fields {
  type Msg = Fields;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Fields> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for FieldsMut<'_> {
  type Msg = Fields;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Fields> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for FieldsMut<'_> {
  type Msg = Fields;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Fields> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for FieldsView<'_> {
  type Msg = Fields;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Fields> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for FieldsMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__UsageSlice__Procedures_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct Procedures {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<Procedures>
}

impl ::protobuf::Message for Procedures {}

impl ::std::default::Default for Procedures {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for Procedures {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `Procedures` is `Sync` because it does not implement interior mutability.
//    Neither does `ProceduresMut`.
unsafe impl Sync for Procedures {}

// SAFETY:
// - `Procedures` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for Procedures {}

impl ::protobuf::Proxied for Procedures {
  type View<'msg> = ProceduresView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for Procedures {}

impl ::protobuf::MutProxied for Procedures {
  type Mut<'msg> = ProceduresMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct ProceduresView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Procedures>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for ProceduresView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for ProceduresView<'msg> {
  type Message = Procedures;
}

impl ::std::fmt::Debug for ProceduresView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for ProceduresView<'_> {
  fn default() -> ProceduresView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, Procedures>> for ProceduresView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Procedures>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> ProceduresView<'msg> {

  pub fn to_owned(&self) -> Procedures {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // callName: optional string
  pub fn callName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // resolvedMethod: optional string
  pub fn resolvedMethod(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // paramTypes: repeated string
  pub fn paramTypes(self) -> ::protobuf::RepeatedView<'msg, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // returnType: optional string
  pub fn returnType(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }

}

// SAFETY:
// - `ProceduresView` is `Sync` because it does not support mutation.
unsafe impl Sync for ProceduresView<'_> {}

// SAFETY:
// - `ProceduresView` is `Send` because while its alive a `ProceduresMut` cannot.
// - `ProceduresView` does not use thread-local data.
unsafe impl Send for ProceduresView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for ProceduresView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for ProceduresView<'msg> {}

impl<'msg> ::protobuf::AsView for ProceduresView<'msg> {
  type Proxied = Procedures;
  fn as_view(&self) -> ::protobuf::View<'msg, Procedures> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ProceduresView<'msg> {
  fn into_view<'shorter>(self) -> ProceduresView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<Procedures> for ProceduresView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Procedures {
    let mut dst = Procedures::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<Procedures> for ProceduresMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Procedures {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for Procedures {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for ProceduresView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for ProceduresMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct ProceduresMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Procedures>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for ProceduresMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for ProceduresMut<'msg> {
  type Message = Procedures;
}

impl ::std::fmt::Debug for ProceduresMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, Procedures>> for ProceduresMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Procedures>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> ProceduresMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, Procedures> {
    self.inner
  }

  pub fn to_owned(&self) -> Procedures {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // callName: optional string
  pub fn callName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_callName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // resolvedMethod: optional string
  pub fn resolvedMethod(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_resolvedMethod(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // paramTypes: repeated string
  pub fn paramTypes(&self) -> ::protobuf::RepeatedView<'_, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn paramTypes_mut(&mut self) -> ::protobuf::RepeatedMut<'_, ::protobuf::ProtoString> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_paramTypes(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<::protobuf::ProtoString>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

  // returnType: optional string
  pub fn returnType(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_returnType(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        4, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        5, val.into()
      )
    }
  }

}

// SAFETY:
// - `ProceduresMut` does not perform any shared mutation.
unsafe impl Send for ProceduresMut<'_> {}

// SAFETY:
// - `ProceduresMut` does not perform any shared mutation.
unsafe impl Sync for ProceduresMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for ProceduresMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for ProceduresMut<'msg> {}

impl<'msg> ::protobuf::AsView for ProceduresMut<'msg> {
  type Proxied = Procedures;
  fn as_view(&self) -> ::protobuf::View<'_, Procedures> {
    ProceduresView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ProceduresMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, Procedures>
  where
      'msg: 'shorter {
    ProceduresView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for ProceduresMut<'msg> {
  type MutProxied = Procedures;
  fn as_mut(&mut self) -> ProceduresMut<'msg> {
    ProceduresMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for ProceduresMut<'msg> {
  fn into_mut<'shorter>(self) -> ProceduresMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl Procedures {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, Procedures> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> ProceduresView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> ProceduresMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // callName: optional string
  pub fn callName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_callName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // resolvedMethod: optional string
  pub fn resolvedMethod(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        1, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_resolvedMethod(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        1,
        view,
      );
    }
  }

  // paramTypes: repeated string
  pub fn paramTypes(&self) -> ::protobuf::RepeatedView<'_, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn paramTypes_mut(&mut self) -> ::protobuf::RepeatedMut<'_, ::protobuf::ProtoString> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_paramTypes(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<::protobuf::ProtoString>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

  // returnType: optional string
  pub fn returnType(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_returnType(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        4, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        5, val.into()
      )
    }
  }

}  // impl Procedures

impl ::std::ops::Drop for Procedures {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for Procedures {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for Procedures {
  type Proxied = Self;
  fn as_view(&self) -> ProceduresView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for Procedures {
  type MutProxied = Self;
  fn as_mut(&mut self) -> ProceduresMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for Procedures {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::usage_slice::atom__UsageSlice__Procedures_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$1X1XET1X)P)P");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::usage_slice::atom__UsageSlice__Procedures_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::usage_slice::atom__UsageSlice__Procedures_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for Procedures {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for Procedures {
  type Msg = Procedures;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Procedures> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for Procedures {
  type Msg = Procedures;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Procedures> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for ProceduresMut<'_> {
  type Msg = Procedures;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Procedures> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ProceduresMut<'_> {
  type Msg = Procedures;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Procedures> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ProceduresView<'_> {
  type Msg = Procedures;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Procedures> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for ProceduresMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__UsageSlice__UserDefinedTypes_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct UserDefinedTypes {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<UserDefinedTypes>
}

impl ::protobuf::Message for UserDefinedTypes {}

impl ::std::default::Default for UserDefinedTypes {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for UserDefinedTypes {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `UserDefinedTypes` is `Sync` because it does not implement interior mutability.
//    Neither does `UserDefinedTypesMut`.
unsafe impl Sync for UserDefinedTypes {}

// SAFETY:
// - `UserDefinedTypes` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for UserDefinedTypes {}

impl ::protobuf::Proxied for UserDefinedTypes {
  type View<'msg> = UserDefinedTypesView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for UserDefinedTypes {}

impl ::protobuf::MutProxied for UserDefinedTypes {
  type Mut<'msg> = UserDefinedTypesMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct UserDefinedTypesView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, UserDefinedTypes>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for UserDefinedTypesView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for UserDefinedTypesView<'msg> {
  type Message = UserDefinedTypes;
}

impl ::std::fmt::Debug for UserDefinedTypesView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for UserDefinedTypesView<'_> {
  fn default() -> UserDefinedTypesView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, UserDefinedTypes>> for UserDefinedTypesView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, UserDefinedTypes>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> UserDefinedTypesView<'msg> {

  pub fn to_owned(&self) -> UserDefinedTypes {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // name: optional string
  pub fn name(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // fields: repeated message atom.UsageSlice.Fields
  pub fn fields(self) -> ::protobuf::RepeatedView<'msg, super::super::usage_slice::Fields> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::Fields>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // procedures: repeated message atom.UsageSlice.Procedures
  pub fn procedures(self) -> ::protobuf::RepeatedView<'msg, super::super::usage_slice::Procedures> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::Procedures>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // fileName: optional string
  pub fn fileName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }

}

// SAFETY:
// - `UserDefinedTypesView` is `Sync` because it does not support mutation.
unsafe impl Sync for UserDefinedTypesView<'_> {}

// SAFETY:
// - `UserDefinedTypesView` is `Send` because while its alive a `UserDefinedTypesMut` cannot.
// - `UserDefinedTypesView` does not use thread-local data.
unsafe impl Send for UserDefinedTypesView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for UserDefinedTypesView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for UserDefinedTypesView<'msg> {}

impl<'msg> ::protobuf::AsView for UserDefinedTypesView<'msg> {
  type Proxied = UserDefinedTypes;
  fn as_view(&self) -> ::protobuf::View<'msg, UserDefinedTypes> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for UserDefinedTypesView<'msg> {
  fn into_view<'shorter>(self) -> UserDefinedTypesView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<UserDefinedTypes> for UserDefinedTypesView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> UserDefinedTypes {
    let mut dst = UserDefinedTypes::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<UserDefinedTypes> for UserDefinedTypesMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> UserDefinedTypes {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for UserDefinedTypes {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for UserDefinedTypesView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for UserDefinedTypesMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct UserDefinedTypesMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, UserDefinedTypes>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for UserDefinedTypesMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for UserDefinedTypesMut<'msg> {
  type Message = UserDefinedTypes;
}

impl ::std::fmt::Debug for UserDefinedTypesMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, UserDefinedTypes>> for UserDefinedTypesMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, UserDefinedTypes>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> UserDefinedTypesMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, UserDefinedTypes> {
    self.inner
  }

  pub fn to_owned(&self) -> UserDefinedTypes {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // name: optional string
  pub fn name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // fields: repeated message atom.UsageSlice.Fields
  pub fn fields(&self) -> ::protobuf::RepeatedView<'_, super::super::usage_slice::Fields> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::Fields>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn fields_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::usage_slice::Fields> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_fields(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::usage_slice::Fields>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

  // procedures: repeated message atom.UsageSlice.Procedures
  pub fn procedures(&self) -> ::protobuf::RepeatedView<'_, super::super::usage_slice::Procedures> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::Procedures>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn procedures_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::usage_slice::Procedures> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_procedures(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::usage_slice::Procedures>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

  // fileName: optional string
  pub fn fileName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_fileName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        4, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        5, val.into()
      )
    }
  }

}

// SAFETY:
// - `UserDefinedTypesMut` does not perform any shared mutation.
unsafe impl Send for UserDefinedTypesMut<'_> {}

// SAFETY:
// - `UserDefinedTypesMut` does not perform any shared mutation.
unsafe impl Sync for UserDefinedTypesMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for UserDefinedTypesMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for UserDefinedTypesMut<'msg> {}

impl<'msg> ::protobuf::AsView for UserDefinedTypesMut<'msg> {
  type Proxied = UserDefinedTypes;
  fn as_view(&self) -> ::protobuf::View<'_, UserDefinedTypes> {
    UserDefinedTypesView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for UserDefinedTypesMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, UserDefinedTypes>
  where
      'msg: 'shorter {
    UserDefinedTypesView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for UserDefinedTypesMut<'msg> {
  type MutProxied = UserDefinedTypes;
  fn as_mut(&mut self) -> UserDefinedTypesMut<'msg> {
    UserDefinedTypesMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for UserDefinedTypesMut<'msg> {
  fn into_mut<'shorter>(self) -> UserDefinedTypesMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl UserDefinedTypes {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, UserDefinedTypes> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> UserDefinedTypesView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> UserDefinedTypesMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // name: optional string
  pub fn name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        0, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        0,
        view,
      );
    }
  }

  // fields: repeated message atom.UsageSlice.Fields
  pub fn fields(&self) -> ::protobuf::RepeatedView<'_, super::super::usage_slice::Fields> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::Fields>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn fields_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::usage_slice::Fields> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_fields(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::usage_slice::Fields>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

  // procedures: repeated message atom.UsageSlice.Procedures
  pub fn procedures(&self) -> ::protobuf::RepeatedView<'_, super::super::usage_slice::Procedures> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        2
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::usage_slice::Procedures>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn procedures_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::usage_slice::Procedures> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        2,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_procedures(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::usage_slice::Procedures>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                2,
                inner.raw());
    }
  }

  // fileName: optional string
  pub fn fileName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_fileName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        4, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        4, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        5, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        5, val.into()
      )
    }
  }

}  // impl UserDefinedTypes

impl ::std::ops::Drop for UserDefinedTypes {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for UserDefinedTypes {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for UserDefinedTypes {
  type Proxied = Self;
  fn as_view(&self) -> UserDefinedTypesView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for UserDefinedTypes {
  type MutProxied = Self;
  fn as_mut(&mut self) -> UserDefinedTypesMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for UserDefinedTypes {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::usage_slice::atom__UsageSlice__UserDefinedTypes_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$1XGG1X)P)P");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::usage_slice::atom__UsageSlice__UserDefinedTypes_msg_init.0, &[<super::super::usage_slice::Fields as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::super::usage_slice::Procedures as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::usage_slice::atom__UsageSlice__UserDefinedTypes_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for UserDefinedTypes {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for UserDefinedTypes {
  type Msg = UserDefinedTypes;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<UserDefinedTypes> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for UserDefinedTypes {
  type Msg = UserDefinedTypes;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<UserDefinedTypes> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for UserDefinedTypesMut<'_> {
  type Msg = UserDefinedTypes;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<UserDefinedTypes> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for UserDefinedTypesMut<'_> {
  type Msg = UserDefinedTypes;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<UserDefinedTypes> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for UserDefinedTypesView<'_> {
  type Msg = UserDefinedTypes;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<UserDefinedTypes> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for UserDefinedTypesMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


#[repr(transparent)]
#[derive(Clone, Copy, PartialEq, Eq, Hash, PartialOrd, Ord)]
pub struct LabelType(i32);

#[allow(non_upper_case_globals)]
impl LabelType {
  pub const Any: LabelType = LabelType(0);
  pub const Local: LabelType = LabelType(1);
  pub const Literal: LabelType = LabelType(2);
  pub const Param: LabelType = LabelType(3);
  pub const Call: LabelType = LabelType(4);
  pub const Identifier: LabelType = LabelType(5);
  pub const TypeRef: LabelType = LabelType(6);
  pub const Unknown: LabelType = LabelType(10);

  fn constant_name(&self) -> ::std::option::Option<&'static str> {
    #[allow(unreachable_patterns)] // In the case of aliases, just emit them all and let the first one match.
    Some(match self.0 {
      0 => "Any",
      1 => "Local",
      2 => "Literal",
      3 => "Param",
      4 => "Call",
      5 => "Identifier",
      6 => "TypeRef",
      10 => "Unknown",
      _ => return None
    })
  }
}

impl ::std::convert::From<LabelType> for i32 {
  fn from(val: LabelType) -> i32 {
    val.0
  }
}

impl ::std::convert::From<i32> for LabelType {
  fn from(val: i32) -> LabelType {
    Self(val)
  }
}

impl ::std::default::Default for LabelType {
  fn default() -> Self {
    Self(0)
  }
}

impl ::std::fmt::Debug for LabelType {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    if let Some(constant_name) = self.constant_name() {
      write!(f, "LabelType::{}", constant_name)
    } else {
      write!(f, "LabelType::from({})", self.0)
    }
  }
}

impl ::protobuf::IntoProxied<i32> for LabelType {
  fn into_proxied(self, _: ::protobuf::__internal::Private) -> i32 {
    self.0
  }
}

impl ::protobuf::__internal::SealedInternal for LabelType {}

impl ::protobuf::Proxied for LabelType {
  type View<'a> = LabelType;
}

impl ::protobuf::Proxy<'_> for LabelType {}
impl ::protobuf::ViewProxy<'_> for LabelType {}

impl ::protobuf::AsView for LabelType {
  type Proxied = LabelType;

  fn as_view(&self) -> LabelType {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for LabelType {
  fn into_view<'shorter>(self) -> LabelType where 'msg: 'shorter {
    self
  }
}

// SAFETY: this is an enum type
unsafe impl ::protobuf::__internal::Enum for LabelType {
  const NAME: &'static str = "LabelType";

  fn is_known(value: i32) -> bool {
    matches!(value, 0|1|2|3|4|5|6|10)
  }
}

impl ::protobuf::__internal::runtime::EntityType for LabelType {
    type Tag = ::protobuf::__internal::runtime::EnumTag;
}


}  // pub mod usage_slice


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__Nodes_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct Nodes {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<Nodes>
}

impl ::protobuf::Message for Nodes {}

impl ::std::default::Default for Nodes {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for Nodes {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `Nodes` is `Sync` because it does not implement interior mutability.
//    Neither does `NodesMut`.
unsafe impl Sync for Nodes {}

// SAFETY:
// - `Nodes` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for Nodes {}

impl ::protobuf::Proxied for Nodes {
  type View<'msg> = NodesView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for Nodes {}

impl ::protobuf::MutProxied for Nodes {
  type Mut<'msg> = NodesMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct NodesView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Nodes>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for NodesView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for NodesView<'msg> {
  type Message = Nodes;
}

impl ::std::fmt::Debug for NodesView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for NodesView<'_> {
  fn default() -> NodesView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, Nodes>> for NodesView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Nodes>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> NodesView<'msg> {

  pub fn to_owned(&self) -> Nodes {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // id: optional uint32
  pub fn id(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        0, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // label: optional enum atom.NodeType
  pub fn label(self) -> super::NodeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        1, (super::NodeType::UnknownNodeType).into()
      ).try_into().unwrap()
    }
  }

  // name: optional string
  pub fn name(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        2, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // fullName: optional string
  pub fn fullName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // signature: optional string
  pub fn signature(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        4, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // isExternal: optional bool
  pub fn isExternal(self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        5, (false).into()
      ).try_into().unwrap()
    }
  }

  // code: optional string
  pub fn code(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        6, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // typeFullName: optional string
  pub fn typeFullName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        7, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // parentMethodName: optional string
  pub fn parentMethodName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        8, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // parentMethodSignature: optional string
  pub fn parentMethodSignature(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        9, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // parentFileName: optional string
  pub fn parentFileName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        10, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // parentPackageName: optional string
  pub fn parentPackageName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        11, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // parentClassName: optional string
  pub fn parentClassName(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        12, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        13, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        14, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // tags: optional string
  pub fn tags(self) -> ::protobuf::View<'msg, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        15, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }

}

// SAFETY:
// - `NodesView` is `Sync` because it does not support mutation.
unsafe impl Sync for NodesView<'_> {}

// SAFETY:
// - `NodesView` is `Send` because while its alive a `NodesMut` cannot.
// - `NodesView` does not use thread-local data.
unsafe impl Send for NodesView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for NodesView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for NodesView<'msg> {}

impl<'msg> ::protobuf::AsView for NodesView<'msg> {
  type Proxied = Nodes;
  fn as_view(&self) -> ::protobuf::View<'msg, Nodes> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for NodesView<'msg> {
  fn into_view<'shorter>(self) -> NodesView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<Nodes> for NodesView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Nodes {
    let mut dst = Nodes::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<Nodes> for NodesMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Nodes {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for Nodes {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for NodesView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for NodesMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct NodesMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Nodes>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for NodesMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for NodesMut<'msg> {
  type Message = Nodes;
}

impl ::std::fmt::Debug for NodesMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, Nodes>> for NodesMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Nodes>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> NodesMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, Nodes> {
    self.inner
  }

  pub fn to_owned(&self) -> Nodes {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // id: optional uint32
  pub fn id(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        0, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_id(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        0, val.into()
      )
    }
  }

  // label: optional enum atom.NodeType
  pub fn label(&self) -> super::NodeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        1, (super::NodeType::UnknownNodeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_label(&mut self, val: super::NodeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        1, val.into()
      )
    }
  }

  // name: optional string
  pub fn name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        2, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        2,
        view,
      );
    }
  }

  // fullName: optional string
  pub fn fullName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_fullName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

  // signature: optional string
  pub fn signature(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        4, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_signature(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        4,
        view,
      );
    }
  }

  // isExternal: optional bool
  pub fn isExternal(&self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        5, (false).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_isExternal(&mut self, val: bool) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_bool_at_index(
        5, val.into()
      )
    }
  }

  // code: optional string
  pub fn code(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        6, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_code(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        6,
        view,
      );
    }
  }

  // typeFullName: optional string
  pub fn typeFullName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        7, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_typeFullName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        7,
        view,
      );
    }
  }

  // parentMethodName: optional string
  pub fn parentMethodName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        8, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_parentMethodName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        8,
        view,
      );
    }
  }

  // parentMethodSignature: optional string
  pub fn parentMethodSignature(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        9, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_parentMethodSignature(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        9,
        view,
      );
    }
  }

  // parentFileName: optional string
  pub fn parentFileName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        10, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_parentFileName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        10,
        view,
      );
    }
  }

  // parentPackageName: optional string
  pub fn parentPackageName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        11, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_parentPackageName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        11,
        view,
      );
    }
  }

  // parentClassName: optional string
  pub fn parentClassName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        12, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_parentClassName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        12,
        view,
      );
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        13, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        13, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        14, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        14, val.into()
      )
    }
  }

  // tags: optional string
  pub fn tags(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        15, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_tags(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        15,
        view,
      );
    }
  }

}

// SAFETY:
// - `NodesMut` does not perform any shared mutation.
unsafe impl Send for NodesMut<'_> {}

// SAFETY:
// - `NodesMut` does not perform any shared mutation.
unsafe impl Sync for NodesMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for NodesMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for NodesMut<'msg> {}

impl<'msg> ::protobuf::AsView for NodesMut<'msg> {
  type Proxied = Nodes;
  fn as_view(&self) -> ::protobuf::View<'_, Nodes> {
    NodesView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for NodesMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, Nodes>
  where
      'msg: 'shorter {
    NodesView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for NodesMut<'msg> {
  type MutProxied = Nodes;
  fn as_mut(&mut self) -> NodesMut<'msg> {
    NodesMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for NodesMut<'msg> {
  fn into_mut<'shorter>(self) -> NodesMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl Nodes {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, Nodes> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> NodesView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> NodesMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // id: optional uint32
  pub fn id(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        0, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_id(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        0, val.into()
      )
    }
  }

  // label: optional enum atom.NodeType
  pub fn label(&self) -> super::NodeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        1, (super::NodeType::UnknownNodeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_label(&mut self, val: super::NodeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        1, val.into()
      )
    }
  }

  // name: optional string
  pub fn name(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        2, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_name(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        2,
        view,
      );
    }
  }

  // fullName: optional string
  pub fn fullName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        3, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_fullName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        3,
        view,
      );
    }
  }

  // signature: optional string
  pub fn signature(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        4, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_signature(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        4,
        view,
      );
    }
  }

  // isExternal: optional bool
  pub fn isExternal(&self) -> bool {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_bool_at_index(
        5, (false).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_isExternal(&mut self, val: bool) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_bool_at_index(
        5, val.into()
      )
    }
  }

  // code: optional string
  pub fn code(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        6, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_code(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        6,
        view,
      );
    }
  }

  // typeFullName: optional string
  pub fn typeFullName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        7, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_typeFullName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        7,
        view,
      );
    }
  }

  // parentMethodName: optional string
  pub fn parentMethodName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        8, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_parentMethodName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        8,
        view,
      );
    }
  }

  // parentMethodSignature: optional string
  pub fn parentMethodSignature(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        9, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_parentMethodSignature(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        9,
        view,
      );
    }
  }

  // parentFileName: optional string
  pub fn parentFileName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        10, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_parentFileName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        10,
        view,
      );
    }
  }

  // parentPackageName: optional string
  pub fn parentPackageName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        11, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_parentPackageName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        11,
        view,
      );
    }
  }

  // parentClassName: optional string
  pub fn parentClassName(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        12, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_parentClassName(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        12,
        view,
      );
    }
  }

  // lineNumber: optional uint32
  pub fn lineNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        13, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_lineNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        13, val.into()
      )
    }
  }

  // columnNumber: optional uint32
  pub fn columnNumber(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        14, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_columnNumber(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        14, val.into()
      )
    }
  }

  // tags: optional string
  pub fn tags(&self) -> ::protobuf::View<'_, ::protobuf::ProtoString> {
    let str_view = unsafe {
      self.inner.ptr().get_string_at_index(
        15, (b"").into()
      )
    };
    // SAFETY: The runtime doesn't require ProtoStr to be UTF-8.
    unsafe { ::protobuf::ProtoStr::from_utf8_unchecked(str_view.as_ref()) }
  }
  pub fn set_tags(&mut self, val: impl ::protobuf::IntoProxied<::protobuf::ProtoString>) {
    let s = val.into_proxied(::protobuf::__internal::Private);
    let (view, arena) =
      s.into_inner(::protobuf::__internal::Private).into_raw_parts();

    let parent_arena = self.inner.arena();
    parent_arena.fuse(&arena);

    unsafe {
      self.inner.ptr_mut().set_base_field_string_at_index(
        15,
        view,
      );
    }
  }

}  // impl Nodes

impl ::std::ops::Drop for Nodes {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for Nodes {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for Nodes {
  type Proxied = Self;
  fn as_view(&self) -> NodesView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for Nodes {
  type MutProxied = Self;
  fn as_mut(&mut self) -> NodesMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for Nodes {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__Nodes_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$)P.P1X1X1X/P1X1X1X1X1X1X1X)P)P1X");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__Nodes_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__Nodes_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for Nodes {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for Nodes {
  type Msg = Nodes;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Nodes> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for Nodes {
  type Msg = Nodes;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Nodes> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for NodesMut<'_> {
  type Msg = Nodes;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Nodes> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for NodesMut<'_> {
  type Msg = Nodes;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Nodes> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for NodesView<'_> {
  type Msg = Nodes;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Nodes> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for NodesMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__DataFlowSlice_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct DataFlowSlice {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<DataFlowSlice>
}

impl ::protobuf::Message for DataFlowSlice {}

impl ::std::default::Default for DataFlowSlice {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for DataFlowSlice {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `DataFlowSlice` is `Sync` because it does not implement interior mutability.
//    Neither does `DataFlowSliceMut`.
unsafe impl Sync for DataFlowSlice {}

// SAFETY:
// - `DataFlowSlice` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for DataFlowSlice {}

impl ::protobuf::Proxied for DataFlowSlice {
  type View<'msg> = DataFlowSliceView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for DataFlowSlice {}

impl ::protobuf::MutProxied for DataFlowSlice {
  type Mut<'msg> = DataFlowSliceMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct DataFlowSliceView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, DataFlowSlice>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for DataFlowSliceView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for DataFlowSliceView<'msg> {
  type Message = DataFlowSlice;
}

impl ::std::fmt::Debug for DataFlowSliceView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for DataFlowSliceView<'_> {
  fn default() -> DataFlowSliceView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, DataFlowSlice>> for DataFlowSliceView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, DataFlowSlice>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> DataFlowSliceView<'msg> {

  pub fn to_owned(&self) -> DataFlowSlice {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // graph: optional message atom.DataFlowSlice.Graph
  pub fn has_graph(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(0)
    }
  }
  pub fn graph_opt(self) -> ::protobuf::Optional<super::data_flow_slice::GraphView<'msg>> {
        ::protobuf::Optional::new(self.graph(), self.has_graph())
  }
  pub fn graph(self) -> super::data_flow_slice::GraphView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(0)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::data_flow_slice::GraphView::default())
  }

  // path: optional message atom.DataFlowSlice.Paths
  pub fn has_path(self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn path_opt(self) -> ::protobuf::Optional<super::data_flow_slice::PathsView<'msg>> {
        ::protobuf::Optional::new(self.path(), self.has_path())
  }
  pub fn path(self) -> super::data_flow_slice::PathsView<'msg> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::data_flow_slice::PathsView::default())
  }

}

// SAFETY:
// - `DataFlowSliceView` is `Sync` because it does not support mutation.
unsafe impl Sync for DataFlowSliceView<'_> {}

// SAFETY:
// - `DataFlowSliceView` is `Send` because while its alive a `DataFlowSliceMut` cannot.
// - `DataFlowSliceView` does not use thread-local data.
unsafe impl Send for DataFlowSliceView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for DataFlowSliceView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for DataFlowSliceView<'msg> {}

impl<'msg> ::protobuf::AsView for DataFlowSliceView<'msg> {
  type Proxied = DataFlowSlice;
  fn as_view(&self) -> ::protobuf::View<'msg, DataFlowSlice> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for DataFlowSliceView<'msg> {
  fn into_view<'shorter>(self) -> DataFlowSliceView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<DataFlowSlice> for DataFlowSliceView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> DataFlowSlice {
    let mut dst = DataFlowSlice::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<DataFlowSlice> for DataFlowSliceMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> DataFlowSlice {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for DataFlowSlice {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for DataFlowSliceView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for DataFlowSliceMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct DataFlowSliceMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, DataFlowSlice>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for DataFlowSliceMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for DataFlowSliceMut<'msg> {
  type Message = DataFlowSlice;
}

impl ::std::fmt::Debug for DataFlowSliceMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, DataFlowSlice>> for DataFlowSliceMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, DataFlowSlice>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> DataFlowSliceMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, DataFlowSlice> {
    self.inner
  }

  pub fn to_owned(&self) -> DataFlowSlice {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // graph: optional message atom.DataFlowSlice.Graph
  pub fn has_graph(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(0)
    }
  }
  pub fn clear_graph(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        0
      );
    }
  }
  pub fn graph_opt(&self) -> ::protobuf::Optional<super::data_flow_slice::GraphView<'_>> {
        ::protobuf::Optional::new(self.graph(), self.has_graph())
  }
  pub fn graph(&self) -> super::data_flow_slice::GraphView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(0)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::data_flow_slice::GraphView::default())
  }
  pub fn graph_mut(&mut self) -> super::data_flow_slice::GraphMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         0, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_graph(&mut self,
    val: impl ::protobuf::IntoProxied<super::data_flow_slice::Graph>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        0,
        val
      );
    }
  }

  // path: optional message atom.DataFlowSlice.Paths
  pub fn has_path(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_path(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn path_opt(&self) -> ::protobuf::Optional<super::data_flow_slice::PathsView<'_>> {
        ::protobuf::Optional::new(self.path(), self.has_path())
  }
  pub fn path(&self) -> super::data_flow_slice::PathsView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::data_flow_slice::PathsView::default())
  }
  pub fn path_mut(&mut self) -> super::data_flow_slice::PathsMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_path(&mut self,
    val: impl ::protobuf::IntoProxied<super::data_flow_slice::Paths>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

}

// SAFETY:
// - `DataFlowSliceMut` does not perform any shared mutation.
unsafe impl Send for DataFlowSliceMut<'_> {}

// SAFETY:
// - `DataFlowSliceMut` does not perform any shared mutation.
unsafe impl Sync for DataFlowSliceMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for DataFlowSliceMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for DataFlowSliceMut<'msg> {}

impl<'msg> ::protobuf::AsView for DataFlowSliceMut<'msg> {
  type Proxied = DataFlowSlice;
  fn as_view(&self) -> ::protobuf::View<'_, DataFlowSlice> {
    DataFlowSliceView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for DataFlowSliceMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, DataFlowSlice>
  where
      'msg: 'shorter {
    DataFlowSliceView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for DataFlowSliceMut<'msg> {
  type MutProxied = DataFlowSlice;
  fn as_mut(&mut self) -> DataFlowSliceMut<'msg> {
    DataFlowSliceMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for DataFlowSliceMut<'msg> {
  fn into_mut<'shorter>(self) -> DataFlowSliceMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl DataFlowSlice {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, DataFlowSlice> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> DataFlowSliceView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> DataFlowSliceMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // graph: optional message atom.DataFlowSlice.Graph
  pub fn has_graph(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(0)
    }
  }
  pub fn clear_graph(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        0
      );
    }
  }
  pub fn graph_opt(&self) -> ::protobuf::Optional<super::data_flow_slice::GraphView<'_>> {
        ::protobuf::Optional::new(self.graph(), self.has_graph())
  }
  pub fn graph(&self) -> super::data_flow_slice::GraphView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(0)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::data_flow_slice::GraphView::default())
  }
  pub fn graph_mut(&mut self) -> super::data_flow_slice::GraphMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         0, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_graph(&mut self,
    val: impl ::protobuf::IntoProxied<super::data_flow_slice::Graph>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        0,
        val
      );
    }
  }

  // path: optional message atom.DataFlowSlice.Paths
  pub fn has_path(&self) -> bool {
    unsafe {
      self.inner.ptr().has_field_at_index(1)
    }
  }
  pub fn clear_path(&mut self) {
    unsafe {
      self.inner.ptr().clear_field_at_index(
        1
      );
    }
  }
  pub fn path_opt(&self) -> ::protobuf::Optional<super::data_flow_slice::PathsView<'_>> {
        ::protobuf::Optional::new(self.path(), self.has_path())
  }
  pub fn path(&self) -> super::data_flow_slice::PathsView<'_> {
    let submsg = unsafe {
      self.inner.ptr().get_message_at_index(1)
    };
    submsg
        .map(|ptr| unsafe { ::protobuf::__internal::runtime::MessageViewInner::wrap(ptr).into() })
       .unwrap_or(super::data_flow_slice::PathsView::default())
  }
  pub fn path_mut(&mut self) -> super::data_flow_slice::PathsMut<'_> {
     let ptr = unsafe {
       self.inner.ptr_mut().get_or_create_mutable_message_at_index(
         1, self.inner.arena()
       ).unwrap()
     };
     ::protobuf::__internal::runtime::MessageMutInner::from_parent(
         self.as_message_mut_inner(::protobuf::__internal::Private),
         ptr
     ).into()
  }
  pub fn set_path(&mut self,
    val: impl ::protobuf::IntoProxied<super::data_flow_slice::Paths>) {

    unsafe {
      ::protobuf::__internal::runtime::message_set_sub_message(
        ::protobuf::AsMut::as_mut(self).inner,
        1,
        val
      );
    }
  }

}  // impl DataFlowSlice

impl ::std::ops::Drop for DataFlowSlice {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for DataFlowSlice {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for DataFlowSlice {
  type Proxied = Self;
  fn as_view(&self) -> DataFlowSliceView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for DataFlowSlice {
  type MutProxied = Self;
  fn as_mut(&mut self) -> DataFlowSliceMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for DataFlowSlice {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__DataFlowSlice_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$33");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__DataFlowSlice_msg_init.0, &[<super::data_flow_slice::Graph as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::data_flow_slice::Paths as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__DataFlowSlice_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for DataFlowSlice {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for DataFlowSlice {
  type Msg = DataFlowSlice;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DataFlowSlice> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for DataFlowSlice {
  type Msg = DataFlowSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DataFlowSlice> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for DataFlowSliceMut<'_> {
  type Msg = DataFlowSlice;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DataFlowSlice> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for DataFlowSliceMut<'_> {
  type Msg = DataFlowSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DataFlowSlice> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for DataFlowSliceView<'_> {
  type Msg = DataFlowSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<DataFlowSlice> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for DataFlowSliceMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

pub mod data_flow_slice {// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__DataFlowSlice__Edges_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct Edges {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<Edges>
}

impl ::protobuf::Message for Edges {}

impl ::std::default::Default for Edges {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for Edges {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `Edges` is `Sync` because it does not implement interior mutability.
//    Neither does `EdgesMut`.
unsafe impl Sync for Edges {}

// SAFETY:
// - `Edges` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for Edges {}

impl ::protobuf::Proxied for Edges {
  type View<'msg> = EdgesView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for Edges {}

impl ::protobuf::MutProxied for Edges {
  type Mut<'msg> = EdgesMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct EdgesView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Edges>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for EdgesView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for EdgesView<'msg> {
  type Message = Edges;
}

impl ::std::fmt::Debug for EdgesView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for EdgesView<'_> {
  fn default() -> EdgesView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, Edges>> for EdgesView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Edges>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> EdgesView<'msg> {

  pub fn to_owned(&self) -> Edges {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // src: optional uint32
  pub fn src(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        0, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // dst: optional uint32
  pub fn dst(self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        1, (0u32).into()
      ).try_into().unwrap()
    }
  }

  // label: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn label(self) -> super::super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (super::super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }

}

// SAFETY:
// - `EdgesView` is `Sync` because it does not support mutation.
unsafe impl Sync for EdgesView<'_> {}

// SAFETY:
// - `EdgesView` is `Send` because while its alive a `EdgesMut` cannot.
// - `EdgesView` does not use thread-local data.
unsafe impl Send for EdgesView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for EdgesView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for EdgesView<'msg> {}

impl<'msg> ::protobuf::AsView for EdgesView<'msg> {
  type Proxied = Edges;
  fn as_view(&self) -> ::protobuf::View<'msg, Edges> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for EdgesView<'msg> {
  fn into_view<'shorter>(self) -> EdgesView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<Edges> for EdgesView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Edges {
    let mut dst = Edges::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<Edges> for EdgesMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Edges {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for Edges {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for EdgesView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for EdgesMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct EdgesMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Edges>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for EdgesMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for EdgesMut<'msg> {
  type Message = Edges;
}

impl ::std::fmt::Debug for EdgesMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, Edges>> for EdgesMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Edges>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> EdgesMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, Edges> {
    self.inner
  }

  pub fn to_owned(&self) -> Edges {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // src: optional uint32
  pub fn src(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        0, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_src(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        0, val.into()
      )
    }
  }

  // dst: optional uint32
  pub fn dst(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        1, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_dst(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        1, val.into()
      )
    }
  }

  // label: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn label(&self) -> super::super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (super::super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_label(&mut self, val: super::super::cpg_struct::edge::EdgeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        2, val.into()
      )
    }
  }

}

// SAFETY:
// - `EdgesMut` does not perform any shared mutation.
unsafe impl Send for EdgesMut<'_> {}

// SAFETY:
// - `EdgesMut` does not perform any shared mutation.
unsafe impl Sync for EdgesMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for EdgesMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for EdgesMut<'msg> {}

impl<'msg> ::protobuf::AsView for EdgesMut<'msg> {
  type Proxied = Edges;
  fn as_view(&self) -> ::protobuf::View<'_, Edges> {
    EdgesView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for EdgesMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, Edges>
  where
      'msg: 'shorter {
    EdgesView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for EdgesMut<'msg> {
  type MutProxied = Edges;
  fn as_mut(&mut self) -> EdgesMut<'msg> {
    EdgesMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for EdgesMut<'msg> {
  fn into_mut<'shorter>(self) -> EdgesMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl Edges {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, Edges> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> EdgesView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> EdgesMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // src: optional uint32
  pub fn src(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        0, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_src(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        0, val.into()
      )
    }
  }

  // dst: optional uint32
  pub fn dst(&self) -> u32 {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_u32_at_index(
        1, (0u32).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_dst(&mut self, val: u32) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_u32_at_index(
        1, val.into()
      )
    }
  }

  // label: optional enum atom.CpgStruct.Edge.EdgeType
  pub fn label(&self) -> super::super::cpg_struct::edge::EdgeType {
    unsafe {
      // TODO: b/361751487: This .into() and .try_into() is only
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      // perfectly (and do an unchecked conversion for
      // i32->enum types, since even for closed enums we trust
      // upb to only return one of the named values).
      self.inner.ptr().get_i32_at_index(
        2, (super::super::cpg_struct::edge::EdgeType::UnknownEdgeType).into()
      ).try_into().unwrap()
    }
  }
  pub fn set_label(&mut self, val: super::super::cpg_struct::edge::EdgeType) {
    unsafe {
      // TODO: b/361751487: This .into() is only here
      // here for the enum<->i32 case, we should avoid it for
      // other primitives where the types naturally match
      //perfectly.
      self.inner.ptr_mut().set_base_field_i32_at_index(
        2, val.into()
      )
    }
  }

}  // impl Edges

impl ::std::ops::Drop for Edges {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for Edges {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for Edges {
  type Proxied = Self;
  fn as_view(&self) -> EdgesView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for Edges {
  type MutProxied = Self;
  fn as_mut(&mut self) -> EdgesMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for Edges {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::data_flow_slice::atom__DataFlowSlice__Edges_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$)P)P.P");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::data_flow_slice::atom__DataFlowSlice__Edges_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::data_flow_slice::atom__DataFlowSlice__Edges_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for Edges {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for Edges {
  type Msg = Edges;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Edges> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for Edges {
  type Msg = Edges;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Edges> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for EdgesMut<'_> {
  type Msg = Edges;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Edges> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for EdgesMut<'_> {
  type Msg = Edges;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Edges> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for EdgesView<'_> {
  type Msg = Edges;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Edges> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for EdgesMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__DataFlowSlice__Flows_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct Flows {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<Flows>
}

impl ::protobuf::Message for Flows {}

impl ::std::default::Default for Flows {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for Flows {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `Flows` is `Sync` because it does not implement interior mutability.
//    Neither does `FlowsMut`.
unsafe impl Sync for Flows {}

// SAFETY:
// - `Flows` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for Flows {}

impl ::protobuf::Proxied for Flows {
  type View<'msg> = FlowsView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for Flows {}

impl ::protobuf::MutProxied for Flows {
  type Mut<'msg> = FlowsMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct FlowsView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Flows>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for FlowsView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for FlowsView<'msg> {
  type Message = Flows;
}

impl ::std::fmt::Debug for FlowsView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for FlowsView<'_> {
  fn default() -> FlowsView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, Flows>> for FlowsView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Flows>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> FlowsView<'msg> {

  pub fn to_owned(&self) -> Flows {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // id: repeated uint32
  pub fn id(self) -> ::protobuf::RepeatedView<'msg, u32> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<u32>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `FlowsView` is `Sync` because it does not support mutation.
unsafe impl Sync for FlowsView<'_> {}

// SAFETY:
// - `FlowsView` is `Send` because while its alive a `FlowsMut` cannot.
// - `FlowsView` does not use thread-local data.
unsafe impl Send for FlowsView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for FlowsView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for FlowsView<'msg> {}

impl<'msg> ::protobuf::AsView for FlowsView<'msg> {
  type Proxied = Flows;
  fn as_view(&self) -> ::protobuf::View<'msg, Flows> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for FlowsView<'msg> {
  fn into_view<'shorter>(self) -> FlowsView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<Flows> for FlowsView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Flows {
    let mut dst = Flows::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<Flows> for FlowsMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Flows {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for Flows {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for FlowsView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for FlowsMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct FlowsMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Flows>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for FlowsMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for FlowsMut<'msg> {
  type Message = Flows;
}

impl ::std::fmt::Debug for FlowsMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, Flows>> for FlowsMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Flows>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> FlowsMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, Flows> {
    self.inner
  }

  pub fn to_owned(&self) -> Flows {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // id: repeated uint32
  pub fn id(&self) -> ::protobuf::RepeatedView<'_, u32> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<u32>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn id_mut(&mut self) -> ::protobuf::RepeatedMut<'_, u32> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_id(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<u32>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}

// SAFETY:
// - `FlowsMut` does not perform any shared mutation.
unsafe impl Send for FlowsMut<'_> {}

// SAFETY:
// - `FlowsMut` does not perform any shared mutation.
unsafe impl Sync for FlowsMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for FlowsMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for FlowsMut<'msg> {}

impl<'msg> ::protobuf::AsView for FlowsMut<'msg> {
  type Proxied = Flows;
  fn as_view(&self) -> ::protobuf::View<'_, Flows> {
    FlowsView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for FlowsMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, Flows>
  where
      'msg: 'shorter {
    FlowsView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for FlowsMut<'msg> {
  type MutProxied = Flows;
  fn as_mut(&mut self) -> FlowsMut<'msg> {
    FlowsMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for FlowsMut<'msg> {
  fn into_mut<'shorter>(self) -> FlowsMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl Flows {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, Flows> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> FlowsView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> FlowsMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // id: repeated uint32
  pub fn id(&self) -> ::protobuf::RepeatedView<'_, u32> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<u32>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn id_mut(&mut self) -> ::protobuf::RepeatedMut<'_, u32> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_id(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<u32>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}  // impl Flows

impl ::std::ops::Drop for Flows {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for Flows {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for Flows {
  type Proxied = Self;
  fn as_view(&self) -> FlowsView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for Flows {
  type MutProxied = Self;
  fn as_mut(&mut self) -> FlowsMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for Flows {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::data_flow_slice::atom__DataFlowSlice__Flows_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$N=");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::data_flow_slice::atom__DataFlowSlice__Flows_msg_init.0, &[], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::data_flow_slice::atom__DataFlowSlice__Flows_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for Flows {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for Flows {
  type Msg = Flows;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Flows> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for Flows {
  type Msg = Flows;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Flows> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for FlowsMut<'_> {
  type Msg = Flows;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Flows> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for FlowsMut<'_> {
  type Msg = Flows;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Flows> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for FlowsView<'_> {
  type Msg = Flows;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Flows> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for FlowsMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__DataFlowSlice__Paths_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct Paths {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<Paths>
}

impl ::protobuf::Message for Paths {}

impl ::std::default::Default for Paths {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for Paths {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `Paths` is `Sync` because it does not implement interior mutability.
//    Neither does `PathsMut`.
unsafe impl Sync for Paths {}

// SAFETY:
// - `Paths` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for Paths {}

impl ::protobuf::Proxied for Paths {
  type View<'msg> = PathsView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for Paths {}

impl ::protobuf::MutProxied for Paths {
  type Mut<'msg> = PathsMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct PathsView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Paths>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for PathsView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for PathsView<'msg> {
  type Message = Paths;
}

impl ::std::fmt::Debug for PathsView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for PathsView<'_> {
  fn default() -> PathsView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, Paths>> for PathsView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Paths>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> PathsView<'msg> {

  pub fn to_owned(&self) -> Paths {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // flows: repeated message atom.DataFlowSlice.Flows
  pub fn flows(self) -> ::protobuf::RepeatedView<'msg, super::super::data_flow_slice::Flows> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::data_flow_slice::Flows>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `PathsView` is `Sync` because it does not support mutation.
unsafe impl Sync for PathsView<'_> {}

// SAFETY:
// - `PathsView` is `Send` because while its alive a `PathsMut` cannot.
// - `PathsView` does not use thread-local data.
unsafe impl Send for PathsView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for PathsView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for PathsView<'msg> {}

impl<'msg> ::protobuf::AsView for PathsView<'msg> {
  type Proxied = Paths;
  fn as_view(&self) -> ::protobuf::View<'msg, Paths> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for PathsView<'msg> {
  fn into_view<'shorter>(self) -> PathsView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<Paths> for PathsView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Paths {
    let mut dst = Paths::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<Paths> for PathsMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Paths {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for Paths {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for PathsView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for PathsMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct PathsMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Paths>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for PathsMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for PathsMut<'msg> {
  type Message = Paths;
}

impl ::std::fmt::Debug for PathsMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, Paths>> for PathsMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Paths>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> PathsMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, Paths> {
    self.inner
  }

  pub fn to_owned(&self) -> Paths {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // flows: repeated message atom.DataFlowSlice.Flows
  pub fn flows(&self) -> ::protobuf::RepeatedView<'_, super::super::data_flow_slice::Flows> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::data_flow_slice::Flows>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn flows_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::data_flow_slice::Flows> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_flows(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::data_flow_slice::Flows>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}

// SAFETY:
// - `PathsMut` does not perform any shared mutation.
unsafe impl Send for PathsMut<'_> {}

// SAFETY:
// - `PathsMut` does not perform any shared mutation.
unsafe impl Sync for PathsMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for PathsMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for PathsMut<'msg> {}

impl<'msg> ::protobuf::AsView for PathsMut<'msg> {
  type Proxied = Paths;
  fn as_view(&self) -> ::protobuf::View<'_, Paths> {
    PathsView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for PathsMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, Paths>
  where
      'msg: 'shorter {
    PathsView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for PathsMut<'msg> {
  type MutProxied = Paths;
  fn as_mut(&mut self) -> PathsMut<'msg> {
    PathsMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for PathsMut<'msg> {
  fn into_mut<'shorter>(self) -> PathsMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl Paths {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, Paths> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> PathsView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> PathsMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // flows: repeated message atom.DataFlowSlice.Flows
  pub fn flows(&self) -> ::protobuf::RepeatedView<'_, super::super::data_flow_slice::Flows> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::data_flow_slice::Flows>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn flows_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::data_flow_slice::Flows> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_flows(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::data_flow_slice::Flows>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}  // impl Paths

impl ::std::ops::Drop for Paths {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for Paths {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for Paths {
  type Proxied = Self;
  fn as_view(&self) -> PathsView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for Paths {
  type MutProxied = Self;
  fn as_mut(&mut self) -> PathsMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for Paths {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::data_flow_slice::atom__DataFlowSlice__Paths_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$G");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::data_flow_slice::atom__DataFlowSlice__Paths_msg_init.0, &[<super::super::data_flow_slice::Flows as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::data_flow_slice::atom__DataFlowSlice__Paths_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for Paths {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for Paths {
  type Msg = Paths;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Paths> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for Paths {
  type Msg = Paths;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Paths> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for PathsMut<'_> {
  type Msg = Paths;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Paths> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for PathsMut<'_> {
  type Msg = Paths;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Paths> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for PathsView<'_> {
  type Msg = Paths;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Paths> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for PathsMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__DataFlowSlice__Graph_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct Graph {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<Graph>
}

impl ::protobuf::Message for Graph {}

impl ::std::default::Default for Graph {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for Graph {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `Graph` is `Sync` because it does not implement interior mutability.
//    Neither does `GraphMut`.
unsafe impl Sync for Graph {}

// SAFETY:
// - `Graph` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for Graph {}

impl ::protobuf::Proxied for Graph {
  type View<'msg> = GraphView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for Graph {}

impl ::protobuf::MutProxied for Graph {
  type Mut<'msg> = GraphMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct GraphView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Graph>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for GraphView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for GraphView<'msg> {
  type Message = Graph;
}

impl ::std::fmt::Debug for GraphView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for GraphView<'_> {
  fn default() -> GraphView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, Graph>> for GraphView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, Graph>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> GraphView<'msg> {

  pub fn to_owned(&self) -> Graph {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // nodes: repeated message atom.Nodes
  pub fn nodes(self) -> ::protobuf::RepeatedView<'msg, super::super::Nodes> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::Nodes>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // edges: repeated message atom.DataFlowSlice.Edges
  pub fn edges(self) -> ::protobuf::RepeatedView<'msg, super::super::data_flow_slice::Edges> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::data_flow_slice::Edges>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `GraphView` is `Sync` because it does not support mutation.
unsafe impl Sync for GraphView<'_> {}

// SAFETY:
// - `GraphView` is `Send` because while its alive a `GraphMut` cannot.
// - `GraphView` does not use thread-local data.
unsafe impl Send for GraphView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for GraphView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for GraphView<'msg> {}

impl<'msg> ::protobuf::AsView for GraphView<'msg> {
  type Proxied = Graph;
  fn as_view(&self) -> ::protobuf::View<'msg, Graph> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for GraphView<'msg> {
  fn into_view<'shorter>(self) -> GraphView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<Graph> for GraphView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Graph {
    let mut dst = Graph::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<Graph> for GraphMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> Graph {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for Graph {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for GraphView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for GraphMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct GraphMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Graph>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for GraphMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for GraphMut<'msg> {
  type Message = Graph;
}

impl ::std::fmt::Debug for GraphMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, Graph>> for GraphMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, Graph>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> GraphMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, Graph> {
    self.inner
  }

  pub fn to_owned(&self) -> Graph {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // nodes: repeated message atom.Nodes
  pub fn nodes(&self) -> ::protobuf::RepeatedView<'_, super::super::Nodes> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::Nodes>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn nodes_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::Nodes> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_nodes(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::Nodes>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

  // edges: repeated message atom.DataFlowSlice.Edges
  pub fn edges(&self) -> ::protobuf::RepeatedView<'_, super::super::data_flow_slice::Edges> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::data_flow_slice::Edges>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn edges_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::data_flow_slice::Edges> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_edges(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::data_flow_slice::Edges>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

}

// SAFETY:
// - `GraphMut` does not perform any shared mutation.
unsafe impl Send for GraphMut<'_> {}

// SAFETY:
// - `GraphMut` does not perform any shared mutation.
unsafe impl Sync for GraphMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for GraphMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for GraphMut<'msg> {}

impl<'msg> ::protobuf::AsView for GraphMut<'msg> {
  type Proxied = Graph;
  fn as_view(&self) -> ::protobuf::View<'_, Graph> {
    GraphView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for GraphMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, Graph>
  where
      'msg: 'shorter {
    GraphView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for GraphMut<'msg> {
  type MutProxied = Graph;
  fn as_mut(&mut self) -> GraphMut<'msg> {
    GraphMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for GraphMut<'msg> {
  fn into_mut<'shorter>(self) -> GraphMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl Graph {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, Graph> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> GraphView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> GraphMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // nodes: repeated message atom.Nodes
  pub fn nodes(&self) -> ::protobuf::RepeatedView<'_, super::super::Nodes> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::Nodes>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn nodes_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::Nodes> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_nodes(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::Nodes>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

  // edges: repeated message atom.DataFlowSlice.Edges
  pub fn edges(&self) -> ::protobuf::RepeatedView<'_, super::super::data_flow_slice::Edges> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::super::data_flow_slice::Edges>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn edges_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::super::data_flow_slice::Edges> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_edges(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::super::data_flow_slice::Edges>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

}  // impl Graph

impl ::std::ops::Drop for Graph {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for Graph {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for Graph {
  type Proxied = Self;
  fn as_view(&self) -> GraphView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for Graph {
  type MutProxied = Self;
  fn as_mut(&mut self) -> GraphMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for Graph {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::super::data_flow_slice::atom__DataFlowSlice__Graph_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$GG");
        ::protobuf::__internal::runtime::link_mini_table(
            super::super::data_flow_slice::atom__DataFlowSlice__Graph_msg_init.0, &[<super::super::Nodes as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            <super::super::data_flow_slice::Edges as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::super::data_flow_slice::atom__DataFlowSlice__Graph_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for Graph {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for Graph {
  type Msg = Graph;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Graph> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for Graph {
  type Msg = Graph;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Graph> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for GraphMut<'_> {
  type Msg = Graph;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Graph> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for GraphMut<'_> {
  type Msg = Graph;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Graph> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for GraphView<'_> {
  type Msg = Graph;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<Graph> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for GraphMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



}  // pub mod data_flow_slice


// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__ReachableSlice_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct ReachableSlice {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<ReachableSlice>
}

impl ::protobuf::Message for ReachableSlice {}

impl ::std::default::Default for ReachableSlice {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for ReachableSlice {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `ReachableSlice` is `Sync` because it does not implement interior mutability.
//    Neither does `ReachableSliceMut`.
unsafe impl Sync for ReachableSlice {}

// SAFETY:
// - `ReachableSlice` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for ReachableSlice {}

impl ::protobuf::Proxied for ReachableSlice {
  type View<'msg> = ReachableSliceView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for ReachableSlice {}

impl ::protobuf::MutProxied for ReachableSlice {
  type Mut<'msg> = ReachableSliceMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct ReachableSliceView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, ReachableSlice>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for ReachableSliceView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for ReachableSliceView<'msg> {
  type Message = ReachableSlice;
}

impl ::std::fmt::Debug for ReachableSliceView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for ReachableSliceView<'_> {
  fn default() -> ReachableSliceView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, ReachableSlice>> for ReachableSliceView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, ReachableSlice>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> ReachableSliceView<'msg> {

  pub fn to_owned(&self) -> ReachableSlice {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // flows: repeated message atom.Nodes
  pub fn flows(self) -> ::protobuf::RepeatedView<'msg, super::Nodes> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::Nodes>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

  // purls: repeated string
  pub fn purls(self) -> ::protobuf::RepeatedView<'msg, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `ReachableSliceView` is `Sync` because it does not support mutation.
unsafe impl Sync for ReachableSliceView<'_> {}

// SAFETY:
// - `ReachableSliceView` is `Send` because while its alive a `ReachableSliceMut` cannot.
// - `ReachableSliceView` does not use thread-local data.
unsafe impl Send for ReachableSliceView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for ReachableSliceView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for ReachableSliceView<'msg> {}

impl<'msg> ::protobuf::AsView for ReachableSliceView<'msg> {
  type Proxied = ReachableSlice;
  fn as_view(&self) -> ::protobuf::View<'msg, ReachableSlice> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ReachableSliceView<'msg> {
  fn into_view<'shorter>(self) -> ReachableSliceView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<ReachableSlice> for ReachableSliceView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> ReachableSlice {
    let mut dst = ReachableSlice::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<ReachableSlice> for ReachableSliceMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> ReachableSlice {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for ReachableSlice {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for ReachableSliceView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for ReachableSliceMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct ReachableSliceMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, ReachableSlice>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for ReachableSliceMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for ReachableSliceMut<'msg> {
  type Message = ReachableSlice;
}

impl ::std::fmt::Debug for ReachableSliceMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, ReachableSlice>> for ReachableSliceMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, ReachableSlice>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> ReachableSliceMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, ReachableSlice> {
    self.inner
  }

  pub fn to_owned(&self) -> ReachableSlice {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // flows: repeated message atom.Nodes
  pub fn flows(&self) -> ::protobuf::RepeatedView<'_, super::Nodes> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::Nodes>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn flows_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::Nodes> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_flows(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::Nodes>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

  // purls: repeated string
  pub fn purls(&self) -> ::protobuf::RepeatedView<'_, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn purls_mut(&mut self) -> ::protobuf::RepeatedMut<'_, ::protobuf::ProtoString> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_purls(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<::protobuf::ProtoString>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

}

// SAFETY:
// - `ReachableSliceMut` does not perform any shared mutation.
unsafe impl Send for ReachableSliceMut<'_> {}

// SAFETY:
// - `ReachableSliceMut` does not perform any shared mutation.
unsafe impl Sync for ReachableSliceMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for ReachableSliceMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for ReachableSliceMut<'msg> {}

impl<'msg> ::protobuf::AsView for ReachableSliceMut<'msg> {
  type Proxied = ReachableSlice;
  fn as_view(&self) -> ::protobuf::View<'_, ReachableSlice> {
    ReachableSliceView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ReachableSliceMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, ReachableSlice>
  where
      'msg: 'shorter {
    ReachableSliceView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for ReachableSliceMut<'msg> {
  type MutProxied = ReachableSlice;
  fn as_mut(&mut self) -> ReachableSliceMut<'msg> {
    ReachableSliceMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for ReachableSliceMut<'msg> {
  fn into_mut<'shorter>(self) -> ReachableSliceMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl ReachableSlice {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, ReachableSlice> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> ReachableSliceView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> ReachableSliceMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // flows: repeated message atom.Nodes
  pub fn flows(&self) -> ::protobuf::RepeatedView<'_, super::Nodes> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::Nodes>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn flows_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::Nodes> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_flows(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::Nodes>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

  // purls: repeated string
  pub fn purls(&self) -> ::protobuf::RepeatedView<'_, ::protobuf::ProtoString> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        1
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<::protobuf::ProtoString>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn purls_mut(&mut self) -> ::protobuf::RepeatedMut<'_, ::protobuf::ProtoString> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        1,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_purls(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<::protobuf::ProtoString>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                1,
                inner.raw());
    }
  }

}  // impl ReachableSlice

impl ::std::ops::Drop for ReachableSlice {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for ReachableSlice {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for ReachableSlice {
  type Proxied = Self;
  fn as_view(&self) -> ReachableSliceView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for ReachableSlice {
  type MutProxied = Self;
  fn as_mut(&mut self) -> ReachableSliceMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for ReachableSlice {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__ReachableSlice_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$GET");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__ReachableSlice_msg_init.0, &[<super::Nodes as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__ReachableSlice_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for ReachableSlice {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for ReachableSlice {
  type Msg = ReachableSlice;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ReachableSlice> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ReachableSlice {
  type Msg = ReachableSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ReachableSlice> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for ReachableSliceMut<'_> {
  type Msg = ReachableSlice;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ReachableSlice> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ReachableSliceMut<'_> {
  type Msg = ReachableSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ReachableSlice> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ReachableSliceView<'_> {
  type Msg = ReachableSlice;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ReachableSlice> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for ReachableSliceMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



// This variable must not be referenced except by protobuf generated
// code.
pub(crate) static mut atom__ReachableSliceList_msg_init: ::protobuf::__internal::runtime::MiniTableInitPtr =
    ::protobuf::__internal::runtime::MiniTableInitPtr(::protobuf::__internal::runtime::MiniTablePtr::dangling());
#[allow(non_camel_case_types)]
pub struct ReachableSliceList {
  inner: ::protobuf::__internal::runtime::OwnedMessageInner<ReachableSliceList>
}

impl ::protobuf::Message for ReachableSliceList {}

impl ::std::default::Default for ReachableSliceList {
  fn default() -> Self {
    Self::new()
  }
}

impl ::std::fmt::Debug for ReachableSliceList {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

// SAFETY:
// - `ReachableSliceList` is `Sync` because it does not implement interior mutability.
//    Neither does `ReachableSliceListMut`.
unsafe impl Sync for ReachableSliceList {}

// SAFETY:
// - `ReachableSliceList` is `Send` because it uniquely owns its arena and does
//   not use thread-local data.
unsafe impl Send for ReachableSliceList {}

impl ::protobuf::Proxied for ReachableSliceList {
  type View<'msg> = ReachableSliceListView<'msg>;
}

impl ::protobuf::__internal::SealedInternal for ReachableSliceList {}

impl ::protobuf::MutProxied for ReachableSliceList {
  type Mut<'msg> = ReachableSliceListMut<'msg>;
}

#[derive(Copy, Clone)]
#[allow(dead_code)]
pub struct ReachableSliceListView<'msg> {
  inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, ReachableSliceList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for ReachableSliceListView<'msg> {}

impl<'msg> ::protobuf::MessageView<'msg> for ReachableSliceListView<'msg> {
  type Message = ReachableSliceList;
}

impl ::std::fmt::Debug for ReachableSliceListView<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl ::std::default::Default for ReachableSliceListView<'_> {
  fn default() -> ReachableSliceListView<'static> {
    ::protobuf::__internal::runtime::MessageViewInner::default().into()
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageViewInner<'msg, ReachableSliceList>> for ReachableSliceListView<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageViewInner<'msg, ReachableSliceList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> ReachableSliceListView<'msg> {

  pub fn to_owned(&self) -> ReachableSliceList {
    ::protobuf::IntoProxied::into_proxied(*self, ::protobuf::__internal::Private)
  }

  // slices: repeated message atom.ReachableSlice
  pub fn slices(self) -> ::protobuf::RepeatedView<'msg, super::ReachableSlice> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::ReachableSlice>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }

}

// SAFETY:
// - `ReachableSliceListView` is `Sync` because it does not support mutation.
unsafe impl Sync for ReachableSliceListView<'_> {}

// SAFETY:
// - `ReachableSliceListView` is `Send` because while its alive a `ReachableSliceListMut` cannot.
// - `ReachableSliceListView` does not use thread-local data.
unsafe impl Send for ReachableSliceListView<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for ReachableSliceListView<'msg> {}
impl<'msg> ::protobuf::ViewProxy<'msg> for ReachableSliceListView<'msg> {}

impl<'msg> ::protobuf::AsView for ReachableSliceListView<'msg> {
  type Proxied = ReachableSliceList;
  fn as_view(&self) -> ::protobuf::View<'msg, ReachableSliceList> {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ReachableSliceListView<'msg> {
  fn into_view<'shorter>(self) -> ReachableSliceListView<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

impl<'msg> ::protobuf::IntoProxied<ReachableSliceList> for ReachableSliceListView<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> ReachableSliceList {
    let mut dst = ReachableSliceList::new();
    assert!(unsafe {
      dst.inner.ptr_mut().deep_copy(self.inner.ptr(), dst.inner.arena())
    });
    dst
  }
}

impl<'msg> ::protobuf::IntoProxied<ReachableSliceList> for ReachableSliceListMut<'msg> {
  fn into_proxied(self, _private: ::protobuf::__internal::Private) -> ReachableSliceList {
    ::protobuf::IntoProxied::into_proxied(::protobuf::IntoView::into_view(self), _private)
  }
}

impl ::protobuf::__internal::runtime::EntityType for ReachableSliceList {
    type Tag = ::protobuf::__internal::runtime::MessageTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for ReachableSliceListView<'msg> {
    type Tag = ::protobuf::__internal::runtime::ViewProxyTag;
}

impl<'msg> ::protobuf::__internal::runtime::EntityType for ReachableSliceListMut<'msg> {
    type Tag = ::protobuf::__internal::runtime::MutProxyTag;
}

#[allow(dead_code)]
#[allow(non_camel_case_types)]
pub struct ReachableSliceListMut<'msg> {
  inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, ReachableSliceList>,
}

impl<'msg> ::protobuf::__internal::SealedInternal for ReachableSliceListMut<'msg> {}

impl<'msg> ::protobuf::MessageMut<'msg> for ReachableSliceListMut<'msg> {
  type Message = ReachableSliceList;
}

impl ::std::fmt::Debug for ReachableSliceListMut<'_> {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    write!(f, "{}", ::protobuf::__internal::runtime::debug_string(self))
  }
}

impl<'msg> From<::protobuf::__internal::runtime::MessageMutInner<'msg, ReachableSliceList>> for ReachableSliceListMut<'msg> {
  fn from(inner: ::protobuf::__internal::runtime::MessageMutInner<'msg, ReachableSliceList>) -> Self {
    Self { inner }
  }
}

#[allow(dead_code)]
impl<'msg> ReachableSliceListMut<'msg> {

  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private)
    -> ::protobuf::__internal::runtime::MessageMutInner<'msg, ReachableSliceList> {
    self.inner
  }

  pub fn to_owned(&self) -> ReachableSliceList {
    ::protobuf::AsView::as_view(self).to_owned()
  }

  // slices: repeated message atom.ReachableSlice
  pub fn slices(&self) -> ::protobuf::RepeatedView<'_, super::ReachableSlice> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::ReachableSlice>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn slices_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::ReachableSlice> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_slices(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::ReachableSlice>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}

// SAFETY:
// - `ReachableSliceListMut` does not perform any shared mutation.
unsafe impl Send for ReachableSliceListMut<'_> {}

// SAFETY:
// - `ReachableSliceListMut` does not perform any shared mutation.
unsafe impl Sync for ReachableSliceListMut<'_> {}

impl<'msg> ::protobuf::Proxy<'msg> for ReachableSliceListMut<'msg> {}
impl<'msg> ::protobuf::MutProxy<'msg> for ReachableSliceListMut<'msg> {}

impl<'msg> ::protobuf::AsView for ReachableSliceListMut<'msg> {
  type Proxied = ReachableSliceList;
  fn as_view(&self) -> ::protobuf::View<'_, ReachableSliceList> {
    ReachableSliceListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ReachableSliceListMut<'msg> {
  fn into_view<'shorter>(self) -> ::protobuf::View<'shorter, ReachableSliceList>
  where
      'msg: 'shorter {
    ReachableSliceListView {
      inner: ::protobuf::__internal::runtime::MessageViewInner::view_of_mut(self.inner.clone())
    }
  }
}

impl<'msg> ::protobuf::AsMut for ReachableSliceListMut<'msg> {
  type MutProxied = ReachableSliceList;
  fn as_mut(&mut self) -> ReachableSliceListMut<'msg> {
    ReachableSliceListMut { inner: self.inner }
  }
}

impl<'msg> ::protobuf::IntoMut<'msg> for ReachableSliceListMut<'msg> {
  fn into_mut<'shorter>(self) -> ReachableSliceListMut<'shorter>
  where
      'msg: 'shorter {
    self
  }
}

#[allow(dead_code)]
impl ReachableSliceList {
  pub fn new() -> Self {
    Self { inner: ::protobuf::__internal::runtime::OwnedMessageInner::<Self>::new() }
  }


  #[doc(hidden)]
  pub fn as_message_mut_inner(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessageMutInner<'_, ReachableSliceList> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner)
  }

  pub fn as_view(&self) -> ReachableSliceListView<'_> {
    ::protobuf::__internal::runtime::MessageViewInner::view_of_owned(&self.inner).into()
  }

  pub fn as_mut(&mut self) -> ReachableSliceListMut<'_> {
    ::protobuf::__internal::runtime::MessageMutInner::mut_of_owned(&mut self.inner).into()
  }

  // slices: repeated message atom.ReachableSlice
  pub fn slices(&self) -> ::protobuf::RepeatedView<'_, super::ReachableSlice> {
    unsafe {
      self.inner.ptr().get_array_at_index(
        0
      )
    }.map_or_else(
        ::protobuf::__internal::runtime::empty_array::<super::ReachableSlice>,
        |raw| unsafe {
          ::protobuf::RepeatedView::from_raw(::protobuf::__internal::Private, raw)
        }
      )
  }
  pub fn slices_mut(&mut self) -> ::protobuf::RepeatedMut<'_, super::ReachableSlice> {
    unsafe {
      let raw_array = self.inner.ptr_mut().get_or_create_mutable_array_at_index(
        0,
        self.inner.arena()
      ).expect("alloc should not fail");
      ::protobuf::RepeatedMut::from_inner(
        ::protobuf::__internal::Private,
        ::protobuf::__internal::runtime::InnerRepeatedMut::new(
          raw_array, self.inner.arena(),
        ),
      )
    }
  }
  pub fn set_slices(&mut self, src: impl ::protobuf::IntoProxied<::protobuf::Repeated<super::ReachableSlice>>) {
    let val = src.into_proxied(::protobuf::__internal::Private);
    let inner = val.inner(::protobuf::__internal::Private);

    self.inner.arena().fuse(inner.arena());
    unsafe {
        <Self as ::protobuf::__internal::runtime::UpbGetMessagePtrMut>::get_ptr_mut(self, ::protobuf::__internal::Private)
            .set_array_at_index(
                0,
                inner.raw());
    }
  }

}  // impl ReachableSliceList

impl ::std::ops::Drop for ReachableSliceList {
  fn drop(&mut self) {
  }
}

impl ::std::clone::Clone for ReachableSliceList {
  fn clone(&self) -> Self {
    self.as_view().to_owned()
  }
}

impl ::protobuf::AsView for ReachableSliceList {
  type Proxied = Self;
  fn as_view(&self) -> ReachableSliceListView<'_> {
    self.as_view()
  }
}

impl ::protobuf::AsMut for ReachableSliceList {
  type MutProxied = Self;
  fn as_mut(&mut self) -> ReachableSliceListMut<'_> {
    self.as_mut()
  }
}

unsafe impl ::protobuf::__internal::runtime::AssociatedMiniTable for ReachableSliceList {
  fn mini_table() -> ::protobuf::__internal::runtime::MiniTablePtr {
    static ONCE_LOCK: ::std::sync::OnceLock<::protobuf::__internal::runtime::MiniTableInitPtr> =
        ::std::sync::OnceLock::new();
    unsafe {
      ONCE_LOCK.get_or_init(|| {
        super::atom__ReachableSliceList_msg_init.0 =
            ::protobuf::__internal::runtime::build_mini_table("$G");
        ::protobuf::__internal::runtime::link_mini_table(
            super::atom__ReachableSliceList_msg_init.0, &[<super::ReachableSlice as ::protobuf::__internal::runtime::AssociatedMiniTable>::mini_table(),
            ], &[]);
        ::protobuf::__internal::runtime::MiniTableInitPtr(super::atom__ReachableSliceList_msg_init.0)
      }).0
    }
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetArena for ReachableSliceList {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for ReachableSliceList {
  type Msg = ReachableSliceList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ReachableSliceList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ReachableSliceList {
  type Msg = ReachableSliceList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ReachableSliceList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtrMut for ReachableSliceListMut<'_> {
  type Msg = ReachableSliceList;
  fn get_ptr_mut(&mut self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ReachableSliceList> {
    self.inner.ptr_mut()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ReachableSliceListMut<'_> {
  type Msg = ReachableSliceList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ReachableSliceList> {
    self.inner.ptr()
  }
}
unsafe impl ::protobuf::__internal::runtime::UpbGetMessagePtr for ReachableSliceListView<'_> {
  type Msg = ReachableSliceList;
  fn get_ptr(&self, _private: ::protobuf::__internal::Private) -> ::protobuf::__internal::runtime::MessagePtr<ReachableSliceList> {
    self.inner.ptr()
  }
}

unsafe impl ::protobuf::__internal::runtime::UpbGetArena for ReachableSliceListMut<'_> {
  fn get_arena(&mut self, _private: ::protobuf::__internal::Private) -> &::protobuf::__internal::runtime::Arena {
    self.inner.arena()
  }
}



#[repr(transparent)]
#[derive(Clone, Copy, PartialEq, Eq, Hash, PartialOrd, Ord)]
pub struct NodePropertyName(i32);

#[allow(non_upper_case_globals)]
impl NodePropertyName {
  pub const UnknownNodeProperty: NodePropertyName = NodePropertyName(0);
  pub const LineNumber: NodePropertyName = NodePropertyName(2);
  pub const ParserTypeName: NodePropertyName = NodePropertyName(3);
  pub const Order: NodePropertyName = NodePropertyName(4);
  pub const Name: NodePropertyName = NodePropertyName(5);
  pub const FullName: NodePropertyName = NodePropertyName(6);
  pub const IsExternal: NodePropertyName = NodePropertyName(7);
  pub const Value: NodePropertyName = NodePropertyName(8);
  pub const ColumnNumber: NodePropertyName = NodePropertyName(11);
  pub const LineNumberEnd: NodePropertyName = NodePropertyName(12);
  pub const Version: NodePropertyName = NodePropertyName(13);
  pub const EvaluationStrategy: NodePropertyName = NodePropertyName(15);
  pub const ColumnNumberEnd: NodePropertyName = NodePropertyName(16);
  pub const Language: NodePropertyName = NodePropertyName(19);
  pub const Content: NodePropertyName = NodePropertyName(20);
  pub const Code: NodePropertyName = NodePropertyName(21);
  pub const Signature: NodePropertyName = NodePropertyName(22);
  pub const DispatchType: NodePropertyName = NodePropertyName(25);
  pub const ModifierType: NodePropertyName = NodePropertyName(26);
  pub const ControlStructureType: NodePropertyName = NodePropertyName(27);
  pub const ArgumentIndex: NodePropertyName = NodePropertyName(40);
  pub const ClosureBindingId: NodePropertyName = NodePropertyName(50);
  pub const TypeFullName: NodePropertyName = NodePropertyName(51);
  pub const TypeDeclFullName: NodePropertyName = NodePropertyName(52);
  pub const InheritsFromTypeFullName: NodePropertyName = NodePropertyName(53);
  pub const MethodFullName: NodePropertyName = NodePropertyName(54);
  pub const AstParentType: NodePropertyName = NodePropertyName(56);
  pub const AstParentFullName: NodePropertyName = NodePropertyName(57);
  pub const DependencyGroupId: NodePropertyName = NodePropertyName(58);
  pub const Symbol: NodePropertyName = NodePropertyName(100);
  pub const MethodShortName: NodePropertyName = NodePropertyName(102);
  pub const PackageName: NodePropertyName = NodePropertyName(103);
  pub const ClassName: NodePropertyName = NodePropertyName(104);
  pub const NodeLabel: NodePropertyName = NodePropertyName(105);
  pub const Filename: NodePropertyName = NodePropertyName(106);
  pub const Overlays: NodePropertyName = NodePropertyName(118);
  pub const Hash: NodePropertyName = NodePropertyName(120);
  pub const ArgumentName: NodePropertyName = NodePropertyName(130);
  pub const Key: NodePropertyName = NodePropertyName(131);
  pub const ClassShortName: NodePropertyName = NodePropertyName(132);
  pub const AliasTypeFullName: NodePropertyName = NodePropertyName(158);
  pub const ClosureOriginalName: NodePropertyName = NodePropertyName(159);
  pub const IsVariadic: NodePropertyName = NodePropertyName(221);
  pub const Root: NodePropertyName = NodePropertyName(1199);
  pub const DynamicTypeHintFullName: NodePropertyName = NodePropertyName(1591);
  pub const Index: NodePropertyName = NodePropertyName(2223);
  pub const CanonicalName: NodePropertyName = NodePropertyName(2001092);
  pub const ContainedRef: NodePropertyName = NodePropertyName(2007161);

  fn constant_name(&self) -> ::std::option::Option<&'static str> {
    #[allow(unreachable_patterns)] // In the case of aliases, just emit them all and let the first one match.
    Some(match self.0 {
      0 => "UnknownNodeProperty",
      2 => "LineNumber",
      3 => "ParserTypeName",
      4 => "Order",
      5 => "Name",
      6 => "FullName",
      7 => "IsExternal",
      8 => "Value",
      11 => "ColumnNumber",
      12 => "LineNumberEnd",
      13 => "Version",
      15 => "EvaluationStrategy",
      16 => "ColumnNumberEnd",
      19 => "Language",
      20 => "Content",
      21 => "Code",
      22 => "Signature",
      25 => "DispatchType",
      26 => "ModifierType",
      27 => "ControlStructureType",
      40 => "ArgumentIndex",
      50 => "ClosureBindingId",
      51 => "TypeFullName",
      52 => "TypeDeclFullName",
      53 => "InheritsFromTypeFullName",
      54 => "MethodFullName",
      56 => "AstParentType",
      57 => "AstParentFullName",
      58 => "DependencyGroupId",
      100 => "Symbol",
      102 => "MethodShortName",
      103 => "PackageName",
      104 => "ClassName",
      105 => "NodeLabel",
      106 => "Filename",
      118 => "Overlays",
      120 => "Hash",
      130 => "ArgumentName",
      131 => "Key",
      132 => "ClassShortName",
      158 => "AliasTypeFullName",
      159 => "ClosureOriginalName",
      221 => "IsVariadic",
      1199 => "Root",
      1591 => "DynamicTypeHintFullName",
      2223 => "Index",
      2001092 => "CanonicalName",
      2007161 => "ContainedRef",
      _ => return None
    })
  }
}

impl ::std::convert::From<NodePropertyName> for i32 {
  fn from(val: NodePropertyName) -> i32 {
    val.0
  }
}

impl ::std::convert::From<i32> for NodePropertyName {
  fn from(val: i32) -> NodePropertyName {
    Self(val)
  }
}

impl ::std::default::Default for NodePropertyName {
  fn default() -> Self {
    Self(0)
  }
}

impl ::std::fmt::Debug for NodePropertyName {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    if let Some(constant_name) = self.constant_name() {
      write!(f, "NodePropertyName::{}", constant_name)
    } else {
      write!(f, "NodePropertyName::from({})", self.0)
    }
  }
}

impl ::protobuf::IntoProxied<i32> for NodePropertyName {
  fn into_proxied(self, _: ::protobuf::__internal::Private) -> i32 {
    self.0
  }
}

impl ::protobuf::__internal::SealedInternal for NodePropertyName {}

impl ::protobuf::Proxied for NodePropertyName {
  type View<'a> = NodePropertyName;
}

impl ::protobuf::Proxy<'_> for NodePropertyName {}
impl ::protobuf::ViewProxy<'_> for NodePropertyName {}

impl ::protobuf::AsView for NodePropertyName {
  type Proxied = NodePropertyName;

  fn as_view(&self) -> NodePropertyName {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for NodePropertyName {
  fn into_view<'shorter>(self) -> NodePropertyName where 'msg: 'shorter {
    self
  }
}

// SAFETY: this is an enum type
unsafe impl ::protobuf::__internal::Enum for NodePropertyName {
  const NAME: &'static str = "NodePropertyName";

  fn is_known(value: i32) -> bool {
    matches!(value, 0|2|3|4|5|6|7|8|11|12|13|15|16|19|20|21|22|25|26|27|40|50|51|52|53|54|56|57|58|100|102|103|104|105|106|118|120|130|131|132|158|159|221|1199|1591|2223|2001092|2007161)
  }
}

impl ::protobuf::__internal::runtime::EntityType for NodePropertyName {
    type Tag = ::protobuf::__internal::runtime::EnumTag;
}


#[repr(transparent)]
#[derive(Clone, Copy, PartialEq, Eq, Hash, PartialOrd, Ord)]
pub struct EdgePropertyName(i32);

#[allow(non_upper_case_globals)]
impl EdgePropertyName {
  pub const UnknownEdgeProperty: EdgePropertyName = EdgePropertyName(0);
  pub const Variable: EdgePropertyName = EdgePropertyName(11);

  fn constant_name(&self) -> ::std::option::Option<&'static str> {
    #[allow(unreachable_patterns)] // In the case of aliases, just emit them all and let the first one match.
    Some(match self.0 {
      0 => "UnknownEdgeProperty",
      11 => "Variable",
      _ => return None
    })
  }
}

impl ::std::convert::From<EdgePropertyName> for i32 {
  fn from(val: EdgePropertyName) -> i32 {
    val.0
  }
}

impl ::std::convert::From<i32> for EdgePropertyName {
  fn from(val: i32) -> EdgePropertyName {
    Self(val)
  }
}

impl ::std::default::Default for EdgePropertyName {
  fn default() -> Self {
    Self(0)
  }
}

impl ::std::fmt::Debug for EdgePropertyName {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    if let Some(constant_name) = self.constant_name() {
      write!(f, "EdgePropertyName::{}", constant_name)
    } else {
      write!(f, "EdgePropertyName::from({})", self.0)
    }
  }
}

impl ::protobuf::IntoProxied<i32> for EdgePropertyName {
  fn into_proxied(self, _: ::protobuf::__internal::Private) -> i32 {
    self.0
  }
}

impl ::protobuf::__internal::SealedInternal for EdgePropertyName {}

impl ::protobuf::Proxied for EdgePropertyName {
  type View<'a> = EdgePropertyName;
}

impl ::protobuf::Proxy<'_> for EdgePropertyName {}
impl ::protobuf::ViewProxy<'_> for EdgePropertyName {}

impl ::protobuf::AsView for EdgePropertyName {
  type Proxied = EdgePropertyName;

  fn as_view(&self) -> EdgePropertyName {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for EdgePropertyName {
  fn into_view<'shorter>(self) -> EdgePropertyName where 'msg: 'shorter {
    self
  }
}

// SAFETY: this is an enum type
unsafe impl ::protobuf::__internal::Enum for EdgePropertyName {
  const NAME: &'static str = "EdgePropertyName";

  fn is_known(value: i32) -> bool {
    matches!(value, 0|11)
  }
}

impl ::protobuf::__internal::runtime::EntityType for EdgePropertyName {
    type Tag = ::protobuf::__internal::runtime::EnumTag;
}


#[repr(transparent)]
#[derive(Clone, Copy, PartialEq, Eq, Hash, PartialOrd, Ord)]
pub struct ModifierTypes(i32);

#[allow(non_upper_case_globals)]
impl ModifierTypes {
  pub const UnknownModifierType: ModifierTypes = ModifierTypes(0);
  pub const Static: ModifierTypes = ModifierTypes(1);
  pub const Public: ModifierTypes = ModifierTypes(2);
  pub const Protected: ModifierTypes = ModifierTypes(3);
  pub const Private: ModifierTypes = ModifierTypes(4);
  pub const Abstract: ModifierTypes = ModifierTypes(5);
  pub const Native: ModifierTypes = ModifierTypes(6);
  pub const Constructor: ModifierTypes = ModifierTypes(7);
  pub const Virtual: ModifierTypes = ModifierTypes(8);
  pub const Internal: ModifierTypes = ModifierTypes(9);
  pub const Final: ModifierTypes = ModifierTypes(10);
  pub const Readonly: ModifierTypes = ModifierTypes(11);

  fn constant_name(&self) -> ::std::option::Option<&'static str> {
    #[allow(unreachable_patterns)] // In the case of aliases, just emit them all and let the first one match.
    Some(match self.0 {
      0 => "UnknownModifierType",
      1 => "Static",
      2 => "Public",
      3 => "Protected",
      4 => "Private",
      5 => "Abstract",
      6 => "Native",
      7 => "Constructor",
      8 => "Virtual",
      9 => "Internal",
      10 => "Final",
      11 => "Readonly",
      _ => return None
    })
  }
}

impl ::std::convert::From<ModifierTypes> for i32 {
  fn from(val: ModifierTypes) -> i32 {
    val.0
  }
}

impl ::std::convert::From<i32> for ModifierTypes {
  fn from(val: i32) -> ModifierTypes {
    Self(val)
  }
}

impl ::std::default::Default for ModifierTypes {
  fn default() -> Self {
    Self(0)
  }
}

impl ::std::fmt::Debug for ModifierTypes {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    if let Some(constant_name) = self.constant_name() {
      write!(f, "ModifierTypes::{}", constant_name)
    } else {
      write!(f, "ModifierTypes::from({})", self.0)
    }
  }
}

impl ::protobuf::IntoProxied<i32> for ModifierTypes {
  fn into_proxied(self, _: ::protobuf::__internal::Private) -> i32 {
    self.0
  }
}

impl ::protobuf::__internal::SealedInternal for ModifierTypes {}

impl ::protobuf::Proxied for ModifierTypes {
  type View<'a> = ModifierTypes;
}

impl ::protobuf::Proxy<'_> for ModifierTypes {}
impl ::protobuf::ViewProxy<'_> for ModifierTypes {}

impl ::protobuf::AsView for ModifierTypes {
  type Proxied = ModifierTypes;

  fn as_view(&self) -> ModifierTypes {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for ModifierTypes {
  fn into_view<'shorter>(self) -> ModifierTypes where 'msg: 'shorter {
    self
  }
}

// SAFETY: this is an enum type
unsafe impl ::protobuf::__internal::Enum for ModifierTypes {
  const NAME: &'static str = "ModifierTypes";

  fn is_known(value: i32) -> bool {
    matches!(value, 0|1|2|3|4|5|6|7|8|9|10|11)
  }
}

impl ::protobuf::__internal::runtime::EntityType for ModifierTypes {
    type Tag = ::protobuf::__internal::runtime::EnumTag;
}


#[repr(transparent)]
#[derive(Clone, Copy, PartialEq, Eq, Hash, PartialOrd, Ord)]
pub struct LANGUAGES(i32);

#[allow(non_upper_case_globals)]
impl LANGUAGES {
  pub const UnknownLanguage: LANGUAGES = LANGUAGES(0);
  pub const Java: LANGUAGES = LANGUAGES(1);
  pub const Javascript: LANGUAGES = LANGUAGES(2);
  pub const Golang: LANGUAGES = LANGUAGES(3);
  pub const Csharp: LANGUAGES = LANGUAGES(4);
  pub const C: LANGUAGES = LANGUAGES(5);
  pub const Python: LANGUAGES = LANGUAGES(6);
  pub const Llvm: LANGUAGES = LANGUAGES(7);
  pub const Php: LANGUAGES = LANGUAGES(8);
  pub const FuzzyTestLang: LANGUAGES = LANGUAGES(9);
  pub const Ghidra: LANGUAGES = LANGUAGES(10);
  pub const Kotlin: LANGUAGES = LANGUAGES(11);
  pub const Newc: LANGUAGES = LANGUAGES(12);
  pub const Javasrc: LANGUAGES = LANGUAGES(13);
  pub const Pythonsrc: LANGUAGES = LANGUAGES(14);
  pub const Jssrc: LANGUAGES = LANGUAGES(15);
  pub const Solidity: LANGUAGES = LANGUAGES(16);
  pub const Rubysrc: LANGUAGES = LANGUAGES(17);

  fn constant_name(&self) -> ::std::option::Option<&'static str> {
    #[allow(unreachable_patterns)] // In the case of aliases, just emit them all and let the first one match.
    Some(match self.0 {
      0 => "UnknownLanguage",
      1 => "Java",
      2 => "Javascript",
      3 => "Golang",
      4 => "Csharp",
      5 => "C",
      6 => "Python",
      7 => "Llvm",
      8 => "Php",
      9 => "FuzzyTestLang",
      10 => "Ghidra",
      11 => "Kotlin",
      12 => "Newc",
      13 => "Javasrc",
      14 => "Pythonsrc",
      15 => "Jssrc",
      16 => "Solidity",
      17 => "Rubysrc",
      _ => return None
    })
  }
}

impl ::std::convert::From<LANGUAGES> for i32 {
  fn from(val: LANGUAGES) -> i32 {
    val.0
  }
}

impl ::std::convert::From<i32> for LANGUAGES {
  fn from(val: i32) -> LANGUAGES {
    Self(val)
  }
}

impl ::std::default::Default for LANGUAGES {
  fn default() -> Self {
    Self(0)
  }
}

impl ::std::fmt::Debug for LANGUAGES {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    if let Some(constant_name) = self.constant_name() {
      write!(f, "LANGUAGES::{}", constant_name)
    } else {
      write!(f, "LANGUAGES::from({})", self.0)
    }
  }
}

impl ::protobuf::IntoProxied<i32> for LANGUAGES {
  fn into_proxied(self, _: ::protobuf::__internal::Private) -> i32 {
    self.0
  }
}

impl ::protobuf::__internal::SealedInternal for LANGUAGES {}

impl ::protobuf::Proxied for LANGUAGES {
  type View<'a> = LANGUAGES;
}

impl ::protobuf::Proxy<'_> for LANGUAGES {}
impl ::protobuf::ViewProxy<'_> for LANGUAGES {}

impl ::protobuf::AsView for LANGUAGES {
  type Proxied = LANGUAGES;

  fn as_view(&self) -> LANGUAGES {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for LANGUAGES {
  fn into_view<'shorter>(self) -> LANGUAGES where 'msg: 'shorter {
    self
  }
}

// SAFETY: this is an enum type
unsafe impl ::protobuf::__internal::Enum for LANGUAGES {
  const NAME: &'static str = "LANGUAGES";

  fn is_known(value: i32) -> bool {
    matches!(value, 0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17)
  }
}

impl ::protobuf::__internal::runtime::EntityType for LANGUAGES {
    type Tag = ::protobuf::__internal::runtime::EnumTag;
}


#[repr(transparent)]
#[derive(Clone, Copy, PartialEq, Eq, Hash, PartialOrd, Ord)]
pub struct EvaluationStrategies(i32);

#[allow(non_upper_case_globals)]
impl EvaluationStrategies {
  pub const UnknownEvaluationStrategy: EvaluationStrategies = EvaluationStrategies(0);
  pub const ByReference: EvaluationStrategies = EvaluationStrategies(1);
  pub const BySharing: EvaluationStrategies = EvaluationStrategies(2);
  pub const ByValue: EvaluationStrategies = EvaluationStrategies(3);

  fn constant_name(&self) -> ::std::option::Option<&'static str> {
    #[allow(unreachable_patterns)] // In the case of aliases, just emit them all and let the first one match.
    Some(match self.0 {
      0 => "UnknownEvaluationStrategy",
      1 => "ByReference",
      2 => "BySharing",
      3 => "ByValue",
      _ => return None
    })
  }
}

impl ::std::convert::From<EvaluationStrategies> for i32 {
  fn from(val: EvaluationStrategies) -> i32 {
    val.0
  }
}

impl ::std::convert::From<i32> for EvaluationStrategies {
  fn from(val: i32) -> EvaluationStrategies {
    Self(val)
  }
}

impl ::std::default::Default for EvaluationStrategies {
  fn default() -> Self {
    Self(0)
  }
}

impl ::std::fmt::Debug for EvaluationStrategies {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    if let Some(constant_name) = self.constant_name() {
      write!(f, "EvaluationStrategies::{}", constant_name)
    } else {
      write!(f, "EvaluationStrategies::from({})", self.0)
    }
  }
}

impl ::protobuf::IntoProxied<i32> for EvaluationStrategies {
  fn into_proxied(self, _: ::protobuf::__internal::Private) -> i32 {
    self.0
  }
}

impl ::protobuf::__internal::SealedInternal for EvaluationStrategies {}

impl ::protobuf::Proxied for EvaluationStrategies {
  type View<'a> = EvaluationStrategies;
}

impl ::protobuf::Proxy<'_> for EvaluationStrategies {}
impl ::protobuf::ViewProxy<'_> for EvaluationStrategies {}

impl ::protobuf::AsView for EvaluationStrategies {
  type Proxied = EvaluationStrategies;

  fn as_view(&self) -> EvaluationStrategies {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for EvaluationStrategies {
  fn into_view<'shorter>(self) -> EvaluationStrategies where 'msg: 'shorter {
    self
  }
}

// SAFETY: this is an enum type
unsafe impl ::protobuf::__internal::Enum for EvaluationStrategies {
  const NAME: &'static str = "EvaluationStrategies";

  fn is_known(value: i32) -> bool {
    matches!(value, 0|1|2|3)
  }
}

impl ::protobuf::__internal::runtime::EntityType for EvaluationStrategies {
    type Tag = ::protobuf::__internal::runtime::EnumTag;
}


#[repr(transparent)]
#[derive(Clone, Copy, PartialEq, Eq, Hash, PartialOrd, Ord)]
pub struct DispatchTypes(i32);

#[allow(non_upper_case_globals)]
impl DispatchTypes {
  pub const UnknownDispatchType: DispatchTypes = DispatchTypes(0);
  pub const StaticDispatch: DispatchTypes = DispatchTypes(1);
  pub const DynamicDispatch: DispatchTypes = DispatchTypes(2);
  pub const Inlined: DispatchTypes = DispatchTypes(3);

  fn constant_name(&self) -> ::std::option::Option<&'static str> {
    #[allow(unreachable_patterns)] // In the case of aliases, just emit them all and let the first one match.
    Some(match self.0 {
      0 => "UnknownDispatchType",
      1 => "StaticDispatch",
      2 => "DynamicDispatch",
      3 => "Inlined",
      _ => return None
    })
  }
}

impl ::std::convert::From<DispatchTypes> for i32 {
  fn from(val: DispatchTypes) -> i32 {
    val.0
  }
}

impl ::std::convert::From<i32> for DispatchTypes {
  fn from(val: i32) -> DispatchTypes {
    Self(val)
  }
}

impl ::std::default::Default for DispatchTypes {
  fn default() -> Self {
    Self(0)
  }
}

impl ::std::fmt::Debug for DispatchTypes {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    if let Some(constant_name) = self.constant_name() {
      write!(f, "DispatchTypes::{}", constant_name)
    } else {
      write!(f, "DispatchTypes::from({})", self.0)
    }
  }
}

impl ::protobuf::IntoProxied<i32> for DispatchTypes {
  fn into_proxied(self, _: ::protobuf::__internal::Private) -> i32 {
    self.0
  }
}

impl ::protobuf::__internal::SealedInternal for DispatchTypes {}

impl ::protobuf::Proxied for DispatchTypes {
  type View<'a> = DispatchTypes;
}

impl ::protobuf::Proxy<'_> for DispatchTypes {}
impl ::protobuf::ViewProxy<'_> for DispatchTypes {}

impl ::protobuf::AsView for DispatchTypes {
  type Proxied = DispatchTypes;

  fn as_view(&self) -> DispatchTypes {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for DispatchTypes {
  fn into_view<'shorter>(self) -> DispatchTypes where 'msg: 'shorter {
    self
  }
}

// SAFETY: this is an enum type
unsafe impl ::protobuf::__internal::Enum for DispatchTypes {
  const NAME: &'static str = "DispatchTypes";

  fn is_known(value: i32) -> bool {
    matches!(value, 0|1|2|3)
  }
}

impl ::protobuf::__internal::runtime::EntityType for DispatchTypes {
    type Tag = ::protobuf::__internal::runtime::EnumTag;
}


#[repr(transparent)]
#[derive(Clone, Copy, PartialEq, Eq, Hash, PartialOrd, Ord)]
pub struct CONTROLSTRUCTURETYPES(i32);

#[allow(non_upper_case_globals)]
impl CONTROLSTRUCTURETYPES {
  pub const UnknownControlStructureType: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(0);
  pub const Break: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(1);
  pub const Continue: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(2);
  pub const While: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(3);
  pub const Do: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(4);
  pub const For: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(5);
  pub const Goto: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(6);
  pub const If: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(7);
  pub const Else: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(8);
  pub const Switch: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(9);
  pub const Try: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(10);
  pub const Throw: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(11);
  pub const Match: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(12);
  pub const Yield: CONTROLSTRUCTURETYPES = CONTROLSTRUCTURETYPES(13);

  fn constant_name(&self) -> ::std::option::Option<&'static str> {
    #[allow(unreachable_patterns)] // In the case of aliases, just emit them all and let the first one match.
    Some(match self.0 {
      0 => "UnknownControlStructureType",
      1 => "Break",
      2 => "Continue",
      3 => "While",
      4 => "Do",
      5 => "For",
      6 => "Goto",
      7 => "If",
      8 => "Else",
      9 => "Switch",
      10 => "Try",
      11 => "Throw",
      12 => "Match",
      13 => "Yield",
      _ => return None
    })
  }
}

impl ::std::convert::From<CONTROLSTRUCTURETYPES> for i32 {
  fn from(val: CONTROLSTRUCTURETYPES) -> i32 {
    val.0
  }
}

impl ::std::convert::From<i32> for CONTROLSTRUCTURETYPES {
  fn from(val: i32) -> CONTROLSTRUCTURETYPES {
    Self(val)
  }
}

impl ::std::default::Default for CONTROLSTRUCTURETYPES {
  fn default() -> Self {
    Self(0)
  }
}

impl ::std::fmt::Debug for CONTROLSTRUCTURETYPES {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    if let Some(constant_name) = self.constant_name() {
      write!(f, "CONTROLSTRUCTURETYPES::{}", constant_name)
    } else {
      write!(f, "CONTROLSTRUCTURETYPES::from({})", self.0)
    }
  }
}

impl ::protobuf::IntoProxied<i32> for CONTROLSTRUCTURETYPES {
  fn into_proxied(self, _: ::protobuf::__internal::Private) -> i32 {
    self.0
  }
}

impl ::protobuf::__internal::SealedInternal for CONTROLSTRUCTURETYPES {}

impl ::protobuf::Proxied for CONTROLSTRUCTURETYPES {
  type View<'a> = CONTROLSTRUCTURETYPES;
}

impl ::protobuf::Proxy<'_> for CONTROLSTRUCTURETYPES {}
impl ::protobuf::ViewProxy<'_> for CONTROLSTRUCTURETYPES {}

impl ::protobuf::AsView for CONTROLSTRUCTURETYPES {
  type Proxied = CONTROLSTRUCTURETYPES;

  fn as_view(&self) -> CONTROLSTRUCTURETYPES {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for CONTROLSTRUCTURETYPES {
  fn into_view<'shorter>(self) -> CONTROLSTRUCTURETYPES where 'msg: 'shorter {
    self
  }
}

// SAFETY: this is an enum type
unsafe impl ::protobuf::__internal::Enum for CONTROLSTRUCTURETYPES {
  const NAME: &'static str = "CONTROLSTRUCTURETYPES";

  fn is_known(value: i32) -> bool {
    matches!(value, 0|1|2|3|4|5|6|7|8|9|10|11|12|13)
  }
}

impl ::protobuf::__internal::runtime::EntityType for CONTROLSTRUCTURETYPES {
    type Tag = ::protobuf::__internal::runtime::EnumTag;
}


#[repr(transparent)]
#[derive(Clone, Copy, PartialEq, Eq, Hash, PartialOrd, Ord)]
pub struct NodeType(i32);

#[allow(non_upper_case_globals)]
impl NodeType {
  pub const UnknownNodeType: NodeType = NodeType(0);
  pub const Method: NodeType = NodeType(1);
  pub const MethodReturn: NodeType = NodeType(3);
  pub const Annotation: NodeType = NodeType(5);
  pub const AnnotationParameterAssign: NodeType = NodeType(6);
  pub const AnnotationParameter: NodeType = NodeType(7);
  pub const Literal: NodeType = NodeType(8);
  pub const Member: NodeType = NodeType(9);
  pub const ArrayInitializer: NodeType = NodeType(14);
  pub const Call: NodeType = NodeType(15);
  pub const Local: NodeType = NodeType(23);
  pub const Tag: NodeType = NodeType(24);
  pub const Location: NodeType = NodeType(25);
  pub const Identifier: NodeType = NodeType(27);
  pub const Return: NodeType = NodeType(30);
  pub const Block: NodeType = NodeType(31);
  pub const MethodParameterOut: NodeType = NodeType(33);
  pub const MethodParameterIn: NodeType = NodeType(34);
  pub const Dependency: NodeType = NodeType(35);
  pub const File: NodeType = NodeType(38);
  pub const MetaData: NodeType = NodeType(39);
  pub const Namespace: NodeType = NodeType(40);
  pub const NamespaceBlock: NodeType = NodeType(41);
  pub const Unknown: NodeType = NodeType(44);
  pub const Type: NodeType = NodeType(45);
  pub const TypeDecl: NodeType = NodeType(46);
  pub const TypeParameter: NodeType = NodeType(47);
  pub const TypeArgument: NodeType = NodeType(48);
  pub const AnnotationLiteral: NodeType = NodeType(49);
  pub const ConfigFile: NodeType = NodeType(50);
  pub const Binding: NodeType = NodeType(146);
  pub const TagNodePair: NodeType = NodeType(208);
  pub const Finding: NodeType = NodeType(214);
  pub const KeyValuePair: NodeType = NodeType(217);
  pub const Modifier: NodeType = NodeType(300);
  pub const MethodRef: NodeType = NodeType(333);
  pub const ClosureBinding: NodeType = NodeType(334);
  pub const TypeRef: NodeType = NodeType(335);
  pub const ControlStructure: NodeType = NodeType(339);
  pub const JumpTarget: NodeType = NodeType(340);
  pub const JumpLabel: NodeType = NodeType(341);
  pub const TemplateDom: NodeType = NodeType(417);
  pub const Comment: NodeType = NodeType(511);
  pub const FieldIdentifier: NodeType = NodeType(2001081);

  fn constant_name(&self) -> ::std::option::Option<&'static str> {
    #[allow(unreachable_patterns)] // In the case of aliases, just emit them all and let the first one match.
    Some(match self.0 {
      0 => "UnknownNodeType",
      1 => "Method",
      3 => "MethodReturn",
      5 => "Annotation",
      6 => "AnnotationParameterAssign",
      7 => "AnnotationParameter",
      8 => "Literal",
      9 => "Member",
      14 => "ArrayInitializer",
      15 => "Call",
      23 => "Local",
      24 => "Tag",
      25 => "Location",
      27 => "Identifier",
      30 => "Return",
      31 => "Block",
      33 => "MethodParameterOut",
      34 => "MethodParameterIn",
      35 => "Dependency",
      38 => "File",
      39 => "MetaData",
      40 => "Namespace",
      41 => "NamespaceBlock",
      44 => "Unknown",
      45 => "Type",
      46 => "TypeDecl",
      47 => "TypeParameter",
      48 => "TypeArgument",
      49 => "AnnotationLiteral",
      50 => "ConfigFile",
      146 => "Binding",
      208 => "TagNodePair",
      214 => "Finding",
      217 => "KeyValuePair",
      300 => "Modifier",
      333 => "MethodRef",
      334 => "ClosureBinding",
      335 => "TypeRef",
      339 => "ControlStructure",
      340 => "JumpTarget",
      341 => "JumpLabel",
      417 => "TemplateDom",
      511 => "Comment",
      2001081 => "FieldIdentifier",
      _ => return None
    })
  }
}

impl ::std::convert::From<NodeType> for i32 {
  fn from(val: NodeType) -> i32 {
    val.0
  }
}

impl ::std::convert::From<i32> for NodeType {
  fn from(val: i32) -> NodeType {
    Self(val)
  }
}

impl ::std::default::Default for NodeType {
  fn default() -> Self {
    Self(0)
  }
}

impl ::std::fmt::Debug for NodeType {
  fn fmt(&self, f: &mut ::std::fmt::Formatter<'_>) -> ::std::fmt::Result {
    if let Some(constant_name) = self.constant_name() {
      write!(f, "NodeType::{}", constant_name)
    } else {
      write!(f, "NodeType::from({})", self.0)
    }
  }
}

impl ::protobuf::IntoProxied<i32> for NodeType {
  fn into_proxied(self, _: ::protobuf::__internal::Private) -> i32 {
    self.0
  }
}

impl ::protobuf::__internal::SealedInternal for NodeType {}

impl ::protobuf::Proxied for NodeType {
  type View<'a> = NodeType;
}

impl ::protobuf::Proxy<'_> for NodeType {}
impl ::protobuf::ViewProxy<'_> for NodeType {}

impl ::protobuf::AsView for NodeType {
  type Proxied = NodeType;

  fn as_view(&self) -> NodeType {
    *self
  }
}

impl<'msg> ::protobuf::IntoView<'msg> for NodeType {
  fn into_view<'shorter>(self) -> NodeType where 'msg: 'shorter {
    self
  }
}

// SAFETY: this is an enum type
unsafe impl ::protobuf::__internal::Enum for NodeType {
  const NAME: &'static str = "NodeType";

  fn is_known(value: i32) -> bool {
    matches!(value, 0|1|3|5|6|7|8|9|14|15|23|24|25|27|30|31|33|34|35|38|39|40|41|44|45|46|47|48|49|50|146|208|214|217|300|333|334|335|339|340|341|417|511|2001081)
  }
}

impl ::protobuf::__internal::runtime::EntityType for NodeType {
    type Tag = ::protobuf::__internal::runtime::EnumTag;
}


